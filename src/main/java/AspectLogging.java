import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class AspectLogging {
@Around("@annotation(annotation.MenuCommand)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable{

    String methodName = joinPoint.getSignature().getName();

    System.out.println("[LOG] شروع اجرای: " + methodName);
    long start = System.currentTimeMillis();

    Object result = joinPoint.proceed();

    long timer = System.currentTimeMillis() - start;
    System.out.println("[LOG] پایان اجرای: " + methodName + " (" + timer + " ms)");

    return result;


}




}
