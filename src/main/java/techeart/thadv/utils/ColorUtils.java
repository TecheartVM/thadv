package techeart.thadv.utils;

public class ColorUtils
{
    public static float[] getRGB(int hex)
    {
        int r = (hex & 0xFF0000) >> 16;
        int g = (hex & 0xFF00) >> 8;
        int b = (hex & 0xFF);
        return new float[] { (float)r/255, (float)g/255, (float)b/255 };
    }
}
