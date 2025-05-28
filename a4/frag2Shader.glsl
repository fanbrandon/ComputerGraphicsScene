#version 430 

uniform int lineColor;
out vec4 color;

void main() {
    if (lineColor == 0) {
        color = vec4(1.0, 0.0, 0.0, 1.0); // Red for X-axis
    } else if (lineColor == 1) {
        color = vec4(0.0, 0.0, 1.0, 1.0); // Blue for Y-axis
    } else if (lineColor == 2) {
        color = vec4(0.0, 1.0, 0.0, 1.0); // Green for Z-axis
    } else {
        color = vec4(0.0, 0.0, 0.0, 1.0); // White for origin (default)
    }
}