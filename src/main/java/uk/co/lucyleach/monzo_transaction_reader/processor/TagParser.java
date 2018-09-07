package uk.co.lucyleach.monzo_transaction_reader.processor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * User: Lucy
 * Date: 20/08/2018
 * Time: 11:50
 */
class TagParser {
  Map<String, Integer> parseTags(String notes, int totalAmount) throws ParsingException {
    if(notes.contains(";")) {
      return parseMultipleTags(notes, totalAmount);
    } else if(notes.isEmpty()) {
      throw new ParsingException("No notes found");
    } else {
      var singleTag = parseSingleTag(notes, totalAmount);
      return Map.of(singleTag, totalAmount);
    }
  }

  private String parseSingleTag(String notes, int totalAmount) throws ParsingException {
    if(notes.trim().contains(" ")) {
      //Try interpreting with amount
      var isNegativeTotal = totalAmount < 0;
      var splitNoteOrException = createSplitNote(notes, isNegativeTotal);
      var splitNote = splitNoteOrException.getResultOrThrow();
      if(splitNote.getAmount() == null) {
        throw new ParsingException("Cannot have single tag using \"rest\" format");
      } else if(splitNote.getAmount() != totalAmount) {
        throw new ParsingException("Amount in " + notes + " does not equal total amount");
      } else {
        return splitNote.getTag();
      }
    } else {
      return notes.replaceFirst("#", "");
    }
  }

  private Map<String, Integer> parseMultipleTags(String notes, int totalAmount) throws ParsingException {
    var isNegativeTotal = totalAmount < 0;
    var splitNotesOrExceptions = Stream.of(notes.split(";"))
        .map(String::trim)
        .map((String note) -> createSplitNote(note, isNegativeTotal))
        .collect(toSet());

    var splitNotes = SplitNoteOrException.throwAllExceptionsOrReturnResults(splitNotesOrExceptions);

    var splitNotesAllWithAmounts = fillInMissingAmountOrFail(totalAmount, splitNotes);
    checkForDuplicateTags(splitNotesAllWithAmounts);
    //noinspection ConstantConditions - we know that getAmount won't be null because the method name says so
    return splitNotesAllWithAmounts.stream().collect(Collectors.toMap(SplitNote::getTag, SplitNote::getAmount));
  }

  private void checkForDuplicateTags(Set<SplitNote> splitNotesAllWithAmounts) throws ParsingException {
    var numDistinctTags = splitNotesAllWithAmounts.stream().map(SplitNote::getTag).collect(toSet()).size();
    if(numDistinctTags != splitNotesAllWithAmounts.size()) {
      throw new ParsingException("Cannot have duplicate tags");
    }
  }

  private Set<SplitNote> fillInMissingAmountOrFail(int totalAmount, Collection<SplitNote> splitNotes) throws ParsingException {
    var splitNotesWithAmount = splitNotes.stream().filter(n -> n.getAmount() != null).collect(toSet());
    var splitNotesWithRest = splitNotes.stream().filter(n -> n.getAmount() == null).collect(toSet());
    //noinspection ConstantConditions - we know that getAmount won't be null because we've just filtered on that
    var totalAmountInNotes = splitNotesWithAmount.stream().mapToInt(SplitNote::getAmount).sum();
    Set<SplitNote> splitNotesAllWithAmounts;
    if(splitNotesWithRest.isEmpty()) {
      if(totalAmount != totalAmountInNotes) {
        throw new ParsingException("Amounts do not sum to total amount");
      } else {
        splitNotesAllWithAmounts = splitNotesWithAmount;
      }
    } else if(splitNotesWithRest.size() > 1) {
      throw new ParsingException("Cannot have more than 1 \"rest\" note");
    } else {
      var restNote = splitNotesWithRest.iterator().next();
      var restAmount = totalAmount - totalAmountInNotes;
      splitNotesAllWithAmounts = new HashSet<>(splitNotesWithAmount);
      splitNotesAllWithAmounts.add(new SplitNote(restNote.tag, restAmount));
    }
    return splitNotesAllWithAmounts;
  }

  private SplitNoteOrException createSplitNote(String note, boolean negativeTotal) {
    var splitNoteArr = note.split(" ");
    if(splitNoteArr.length != 2) {
      return SplitNoteOrException.createException(new ParsingException("Note " + note + " is in incorrect format"));
    }

    var tag = splitNoteArr[1].replaceFirst("#", "");

    if(splitNoteArr[0].equals("rest")) {
      return SplitNoteOrException.createResult(new SplitNote(tag, null));
    } else {
      try {
        var positivePoundAmount = Double.parseDouble(splitNoteArr[0]);
        var amount = switchAmountToPence(positivePoundAmount);
        if(negativeTotal) {
          amount = -1 * amount;
        }
        return SplitNoteOrException.createResult(new SplitNote(tag, amount));
      } catch(NumberFormatException e) {
        return SplitNoteOrException.createException(new ParsingException(note + " does not start with either rest or an amount"));
      }
    }
  }

  private static int switchAmountToPence(double poundAmount) {
    return (int) Math.round(poundAmount * 100d);
  }

  class SplitNote {
    private final String tag;
    @Nullable
    private final Integer amount;

    SplitNote(String tag, @Nullable Integer amount) {
      this.tag = tag;
      this.amount = amount;
    }

    String getTag() {
      return tag;
    }

    @Nullable
    Integer getAmount() {
      return amount;
    }
  }
}
