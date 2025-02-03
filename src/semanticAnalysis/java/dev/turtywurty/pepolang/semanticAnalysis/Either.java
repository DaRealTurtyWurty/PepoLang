package dev.turtywurty.pepolang.semanticAnalysis;

import java.util.Objects;
import java.util.function.Function;

public class Either<L, R> {
    private final L left;
    private final R right;

    private Either(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Either<L, R> left(L left) {
        return new Either<>(left, null);
    }

    public static <L, R> Either<L, R> right(R right) {
        return new Either<>(null, right);
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return right != null;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public <T> T map(Function<L, T> leftFunction, Function<R, T> rightFunction) {
        if (isLeft()) {
            return leftFunction.apply(left);
        } else {
            return rightFunction.apply(right);
        }
    }

    public <T> T mapLeft(Function<L, T> leftFunction) {
        if (isLeft()) {
            return leftFunction.apply(left);
        } else {
            throw new IllegalStateException("Cannot map left value when right value is present!");
        }
    }

    public <T> T mapRight(Function<R, T> rightFunction) {
        if (isRight()) {
            return rightFunction.apply(right);
        } else {
            throw new IllegalStateException("Cannot map right value when left value is present!");
        }
    }

    public void ifLeft(Runnable leftRunnable) {
        ifLeft(leftRunnable, () -> {});
    }

    public void ifRight(Runnable rightRunnable) {
        ifRight(rightRunnable, () -> {});
    }

    public void ifLeft(Runnable leftRunnable, Runnable rightRunnable) {
        if (isLeft()) {
            leftRunnable.run();
        } else {
            rightRunnable.run();
        }
    }

    public void ifRight(Runnable rightRunnable, Runnable leftRunnable) {
        if (isRight()) {
            rightRunnable.run();
        } else {
            leftRunnable.run();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Either<?, ?> either = (Either<?, ?>) o;
        return Objects.equals(left, either.left) && Objects.equals(right, either.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "Either{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}
