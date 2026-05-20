package dev.merosssany.cinematica.core.data;

import static org.joml.Math.clamp;

/**
 * Represents a color with Red, Green, Blue, and Alpha (transparency) components.
 * <p>
 * This class uses a normalized float range (0.0 to 1.0) for all channels,
 * which is the standard format expected by OpenGL shaders. It provides
 * utility methods for accessibility, such as automatic contrast calculation.
 * </p>
 *
 *
 *
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>Normalization:</b> Includes a static helper to convert standard 0-255
 * integer color values into the 0.0-1.0 float format.</li>
 * <li><b>Perceptual Contrast:</b> {@code getContrastColor} uses a weighted
 * luminosity formula to determine if white or black text would be more
 * readable over this color.</li>
 * <li><b>Fluent API:</b> Methods like {@code setAlpha()} return the {@code RGBA}
 * instance, allowing for method chaining (e.g., {@code myColor.setAlpha(0.5f).add(0.1f)}).</li>
 * </ul>
 */
public class RGBA extends RGB {
    protected float alpha = 0;
    
    public RGBA(int red, int green, int blue, float alpha) {
        this(red / 255f, green / 255f, blue / 255f, alpha);
    }
    
    public RGBA() {
        super();
    }
    
    public RGBA(float red, float green, float blue, float alpha) {
        super(red, green, blue);
        this.alpha = clamp(0, 1, alpha);
    }
    
    public RGBA(RGBA color) {
        super(color);
        alpha = color.alpha;
    }
    
    public RGBA(RGB rgb, float alpha) {
        this.set(rgb);
        this.alpha = clamp(0, 1, alpha);
    }
    
    public static RGBA fromRGBA(int red, int green, int blue, int alpha) {
        float r = (float) red / 255f;
        float g = (float) green / 255f;
        float b = (float) blue / 255f;
        return new RGBA(r, g, b, alpha / 255f);
    }
    
    public static RGBA getContrastColor(float red, float green, float blue, float alpha) {
        float luminosity = (0.2126f * red) +
                (0.7152f * green) +
                (0.0722f * blue);
        
        float threshold = 0.5f;
        
        if (luminosity < threshold) {
            return new RGBA(1.0f, 1.0f, 1.0f, alpha);
        } else {
            return new RGBA(0.0f, 0.0f, 0.0f, alpha);
        }
    }
    
    /**
     * Calculates a high-contrast foreground color (White or Black) based on
     * the perceived brightness of the background.
     * <p>
     * Formula: {@code 0.2126*R + 0.7152*G + 0.0722*B}
     * </p>
     */
    public static RGBA getContrastColor(RGBA rgba) {
        return getContrastColor(rgba.r(),rgba.g(),rgba.b(),rgba.alpha);
    }
    
    // IDK, but I guess someone is lazy enough to type new RGBA()
    public static RGBA copy(RGBA color) {
        return new RGBA(color);
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    public RGBA setAlpha(float alpha) {
        this.alpha = clamp(0, 1, alpha);
        return this;
    }
    
    @Override
    public RGBA add(float num) {
        super.add(num);
        this.alpha = clamp(0, 1, alpha + num);
        return this;
    }
    
    public RGBA set(float r, float g, float b, float a) {
        this.red = clamp(0, 1, r);
        this.green = clamp(0, 1, g);
        this.blue = clamp(0, 1, b);
        this.alpha = clamp(0, 1, a);
        return this;
    }
    
    public RGBA set(RGBA color) {
        red = color.red;
        green = color.green;
        blue = color.blue;
        alpha = color.alpha;
        return this;
    }
    
    public float a() {
        return getAlpha();
    }
    
    public RGBA a(float a) {
        return setAlpha(a);
    }
    
    /**
     * Calculates a high-contrast foreground color (White or Black) based on
     * the perceived brightness of the background.
     * <p>
     * Formula: {@code 0.2126*R + 0.7152*G + 0.0722*B}
     * </p>
     */
    @Override
    public RGBA getContrastColor() {
        RGB rgb = super.getContrastColor();
        return new RGBA(rgb.red, rgb.green, rgb.blue, a());
    }
    
    @Override
    public RGBA copy() {
        return new RGBA(super.copy(), alpha);
    }
    
    public RGBA addNew(float r, float g, float b) {
        return new RGBA(this.red + r, this.green + g, this.blue + b, this.alpha);
    }
    
    @Override
    public RGBA addNew(float v) {
        return addNew(v,v,v);
    }
    
    public int getHexAlpha() {
        return (int) (a() * 255);
    }
    
    public int toHexadecimalARGB() {
        // Scale the normalized 0.0-1.0 floats back to 0-255 integers
        int a = Math.round(clamp(0, 1, alpha) * 255f);
        int r = Math.round(clamp(0, 1, red) * 255f);
        int g = Math.round(clamp(0, 1, green) * 255f);
        int b = Math.round(clamp(0, 1, blue) * 255f);
        
        // Format: 0xAARRGGBB
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    public RGBA getLightnessInvertedColor() {
        return new RGBA(RGB.getLightnessInvertedColor(r(), g(), b()), a());
    }
}
