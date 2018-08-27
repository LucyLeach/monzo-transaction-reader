package uk.co.lucyleach.monzo_transaction_reader.processor;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;

/**
 * User: Lucy
 * Date: 27/08/2018
 * Time: 13:23
 */
public class ResultOrException<RES> {
  private final Either<RES, ParsingException> either;

  private ResultOrException(Either<RES, ParsingException> either) {
    this.either = either;
  }

  public static <RES> ResultOrException<RES> createResult(RES res) {
    checkNotNull(res);
    return new ResultOrException<>(Either.createLeft(res));
  }

  public static <RES> ResultOrException<RES> createException(ParsingException exception) {
    checkNotNull(exception);
    return new ResultOrException<>(Either.createRight(exception));
  }

  public boolean isSuccess() {
    return either.getLeft() != null;
  }

  public boolean isException() {
    return !isSuccess();
  }

  public RES getResult() {
    return either.getLeft();
  }

  public ParsingException getException() {
    return either.getRight();
  }

  public RES getResultOrThrow() throws ParsingException {
    if(isSuccess()) {
      return getResult();
    } else {
      throw getException();
    }
  }

  public <RES2> ResultOrException<RES2> mapSuccess(Function<RES, RES2> function) {
    return new ResultOrException<>(either.map(function, Function.identity()));
  }

  public static <RES> Collection<RES> throwAllExceptionsOrReturnResults(Collection<ResultOrException<RES>> resultsOrExceptions) throws ParsingException {
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
