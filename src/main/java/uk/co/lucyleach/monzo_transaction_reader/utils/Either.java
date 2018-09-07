package uk.co.lucyleach.monzo_transaction_reader.utils;

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

  public L getLeft() {
    return left;
  }

  public R getRight() {
    return right;
  }

  public boolean isLeft() {
    return left != null;
  }

  public static <L, R> Either<L, R> createLeft(L left) {
    checkNotNull(left);
    return new Either<>(left, null);
  }

  public static <L, R> Either<L, R> createRight(R right) {
    checkNotNull(right);
    return new Either<>(null, right);
  }
}
