/**
 * Thrown by {@link Military#shoot()} when it is asked to fire but there is no
 * live zombie within firing range.
 *
 * <p>It extends {@code Exception} (not {@code RuntimeException}), so it is a
 * <em>checked</em> exception: any code that calls {@code shoot()} must either
 * declare {@code throws NoTargetException} or catch it. {@code Military.update()}
 * catches it every tick - "nothing to shoot" is a normal situation, not a bug.
 */
public class NoTargetException extends Exception {

    public NoTargetException(String message) {
        super(message);   // pass the human-readable reason up to Exception
    }
}