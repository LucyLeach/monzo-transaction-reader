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
class ResultOrException<RES> {
  private final Either<RES, ParsingException> either;

  private ResultOrException(Either<RES, ParsingException> either) {
    this.either = either;
  }

  static <RES> ResultOrException<RES> createResult(RES res) {
    checkNotNull(res);
    return new ResultOrException<>(Either.createLeft(res));
  }

  static <RES> ResultOrException<RES> createException(ParsingException exception) {
    checkNotNull(exception);
    return new ResultOrException<>(Either.createRight(exception));
  }

  boolean isSuccess() {
    return either.getLeft() != null;
  }

  boolean isException() {
    return !isSuccess();
  }

  RES getResult() {
    return either.getLeft();
  }

  ParsingException getException() {
    return either.getRight();
  }

  RES getResultOrThrow() throws ParsingException {
    if(isSuccess()) {
      return getResult();
    } else {
      throw getException();
    }
  }

  static <RES> Collection<RES> throwAllExceptionsOrReturnResults(Collection<ResultOrException<RES>> resultsOrExceptions) throws ParsingException {
    if(resultsOrExceptions.stream().anyMatch(ResultOrException::isException)) {
      var exceptionMessages = resultsOrExceptions.stream()
          .filter(ResultOrException::isException)
          .map(roe -> roe.getException().getMessage())
          .collect(Collectors.joining("; "));
      throw new ParsingException("Multiple errors: " + exceptionMessages);
    } else {
      return resultsOrExceptions.stream().map(ResultOrException::getResult).collect(toSet());
    }
  }
}
