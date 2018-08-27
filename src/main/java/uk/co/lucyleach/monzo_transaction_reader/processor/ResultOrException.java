package uk.co.lucyleach.monzo_transaction_reader.processor;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

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

  public RES getResult() {
    return either.getLeft();
  }

  public ParsingException getException() {
    return either.getRight();
  }

  public <RES2> ResultOrException<RES2> mapSuccess(Function<RES, RES2> function) {
    return new ResultOrException<>(either.map(function, Function.identity()));
  }
}
