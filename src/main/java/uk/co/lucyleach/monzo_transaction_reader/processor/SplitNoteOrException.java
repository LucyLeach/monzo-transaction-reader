package uk.co.lucyleach.monzo_transaction_reader.processor;

import uk.co.lucyleach.monzo_transaction_reader.utils.Either;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;

/**
 * User: Lucy
 * Date: 27/08/2018
 * Time: 13:23
 */
class SplitNoteOrException {
  private final Either<TagParser.SplitNote, ParsingException> either;

  private SplitNoteOrException(Either<TagParser.SplitNote, ParsingException> either) {
    this.either = either;
  }

  boolean isSuccess() {
    return either.isLeft();
  }

  boolean isException() {
    return !isSuccess();
  }

  TagParser.SplitNote getResult() {
    return either.getLeft();
  }

  ParsingException getException() {
    return either.getRight();
  }

  TagParser.SplitNote getResultOrThrow() throws ParsingException {
    if(isSuccess()) {
      return getResult();
    } else {
      throw getException();
    }
  }

  static SplitNoteOrException createResult(TagParser.SplitNote splitNote) {
    checkNotNull(splitNote);
    return new SplitNoteOrException(Either.createLeft(splitNote));
  }

  static SplitNoteOrException createException(ParsingException exception) {
    checkNotNull(exception);
    return new SplitNoteOrException(Either.createRight(exception));
  }

  static Collection<TagParser.SplitNote> throwAllExceptionsOrReturnResults(Collection<SplitNoteOrException> resultsOrExceptions) throws ParsingException {
    var numExceptions = resultsOrExceptions.stream().filter(SplitNoteOrException::isException).count();
    if(numExceptions > 0) {
      var exceptionMessages = resultsOrExceptions.stream()
          .filter(SplitNoteOrException::isException)
          .map(roe -> roe.getException().getMessage())
          .collect(Collectors.joining("; "));
      throw new ParsingException(numExceptions > 1 ? "Multiple errors: " + exceptionMessages : exceptionMessages);
    } else {
      return resultsOrExceptions.stream().map(SplitNoteOrException::getResult).collect(toSet());
    }
  }
}
