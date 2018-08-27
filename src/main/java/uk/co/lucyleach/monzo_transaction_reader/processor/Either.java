package uk.co.lucyleach.monzo_transaction_reader.processor;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: Lucy
 * Date: 27/08/2018
 * Time: 13:09
 */
public class Either<L, R> {
  private final L left;
  private final R right;

  private Either(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public static <L, R> Either<L, R> createLeft(L left) {
    checkNotNull(left);
    return new Either<>(left, null);
  }

  public static <L, R> Either<L, R> createRight(R right) {
    checkNotNull(right);
    return new Either<>(null, right);
  }

  public L getLeft() {
    return left;
  }

  public R getRight() {
    return right;
  }

  public <L2, R2> Either<L2, R2> map(Function<? super L, ? extends L2> leftFunction, Function<? super R, ? extends R2> rightFunction) {
    if(left != null) {
      return createLeft(leftFunction.apply(left));
    } else { //By construction, right is not null
      return createRight(rightFunction.apply(right));
    }
  }
}
