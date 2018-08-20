package uk.co.lucyleach.monzo_transaction_reader.processor;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
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
    } else {
      var singleTag = parseSingleTag(notes);
      return Map.of(singleTag, totalAmount);
    }
  }

  private String parseSingleTag(String notes) throws ParsingException {
    if(!notes.startsWith("#")) {
      throw new ParsingException("No hash found");
    } else if (notes.trim().contains(" ")) {
      throw new ParsingException("More than one word in a single tag");
    } else {
      return notes.replaceFirst("#", "");
    }
  }

  private Map<String, Integer> parseMultipleTags(String notes, int totalAmount) throws ParsingException {
    var errors = new HashSet<String>();
    var splitNotes = Stream.of(notes.split(";"))
        .map(String::trim)
        .map(note -> createSplitNoteOrNull(note, errors))
        .filter(Objects::nonNull)
        .collect(toSet());
    if(!errors.isEmpty()) {
      throw new ParsingException("Error(s) occurred reading the notes " + notes + ": " + String.join(", ", errors));
    }

    Set<SplitNote> splitNotesAllWithAmounts = fillInMissingAmountOrFail(totalAmount, splitNotes);
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

  private Set<SplitNote> fillInMissingAmountOrFail(int totalAmount, Set<SplitNote> splitNotes) throws ParsingException {
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

  private SplitNote createSplitNoteOrNull(String note, Set<String> errorsToAddTo) {
    var splitNoteArr = note.split(" ");
    if(splitNoteArr.length != 2) {
      errorsToAddTo.add("Note " + note + " is in incorrect format");
      return null;
    }

    String tag;
    if(!splitNoteArr[1].startsWith("#")) {
      errorsToAddTo.add("Note " + note + " is in incorrect format");
      return null;
    } else {
      tag = splitNoteArr[1].replaceFirst("#", "");
    }

    if(splitNoteArr[0].equals("rest")) {
      return new SplitNote(tag, null);
    } else {
      try {
        var positivePoundAmount = Double.parseDouble(splitNoteArr[0]);
        var amount = (int) Math.round(positivePoundAmount * -100d);
        return new SplitNote(tag, amount);
      } catch(NumberFormatException e) {
        errorsToAddTo.add(note + " does not start with either rest or an amount");
        return null;
      }
    }
  }

  class ParsingException extends Exception {
    ParsingException(String message) {
      super(message);
    }
  }

  class SplitNote {
    private final String tag;
    @Nullable private final Integer amount;

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
