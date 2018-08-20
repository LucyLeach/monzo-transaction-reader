package uk.co.lucyleach.monzo_transaction_reader.processor;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    var notesWithAmount = new HashSet<SplitNote>();
    var notesWithoutAmount = new HashSet<SplitNote>();
    for(var note: notes.split(";")) {
      var trimmedNote = note.trim();
      var splitNoteArr = trimmedNote.split(" ");
      if(splitNoteArr.length != 2) {
        throw new ParsingException("Note " + trimmedNote + " is in incorrect format");
      }

      String tag;
      if(!splitNoteArr[1].startsWith("#")) {
        throw new ParsingException("Note " + trimmedNote + " is in incorrect format");
      } else {
        tag = splitNoteArr[1].replaceFirst("#", "");
      }

      if(splitNoteArr[0].equals("rest")) {
        notesWithoutAmount.add(new SplitNote(tag, null));
      } else {
        try {
          var positivePoundAmount = Double.parseDouble(splitNoteArr[0]);
          var amount = (int) Math.round(positivePoundAmount * -100d);
          notesWithAmount.add(new SplitNote(tag, amount));
        } catch(NumberFormatException e) {
          throw new ParsingException(note + " does not start with either rest or an amount");
        }
      }
    }

    var totalAmountInNotes = notesWithAmount.stream().mapToInt(sn -> sn.amount).sum();
    Set<SplitNote> completeNotes;
    if(notesWithoutAmount.size() > 1) {
      throw new ParsingException("Cannot have more than 1 \"rest\" note");
    } else if (notesWithoutAmount.isEmpty()) {
      if(totalAmount != totalAmountInNotes) {
        throw new ParsingException("Amounts do not sum to total amount");
      }
      completeNotes = notesWithAmount;
    } else {
      var restNote = notesWithoutAmount.iterator().next();
      var restAmount = totalAmount - totalAmountInNotes;
      completeNotes = new HashSet<>(notesWithAmount);
      completeNotes.add(new SplitNote(restNote.tag, restAmount));
    }

    var numDistinctTags = completeNotes.stream().map(SplitNote::getTag).collect(toSet()).size();
    if(numDistinctTags != completeNotes.size()) {
      throw new ParsingException("Cannot have duplicate tags");
    }

    return completeNotes.stream().collect(Collectors.toMap(SplitNote::getTag, SplitNote::getAmount));
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
