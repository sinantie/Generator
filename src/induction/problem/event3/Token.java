package induction.problem.event3;

/**
 *
 * @author konstas
 */
public class Token
{
    char tchar;
    String fieldName, value;
    int role;

    public Token(char tchar, String fieldName, int role, String value)
    {
        this.fieldName = fieldName;
        this.role = role;
        this.tchar = tchar;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return tchar + fieldName + ":" + value;
    }


}
