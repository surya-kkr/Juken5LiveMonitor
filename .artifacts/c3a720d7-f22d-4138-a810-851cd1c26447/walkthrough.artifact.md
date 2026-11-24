# Walkthrough - Connection Guide Dialog Optimization

I have updated the **Panduan Koneksi ECU** (Connection Guide) dialog to make it more compact and easier to read.

## Changes Made

### 1. Square Aspect Ratio
- **Fixed Size**: Changed the dialog from auto-height (which was very long) to a fixed square size of **420dp x 420dp**.
- **Internal Scrolling**: Added a dedicated scrollable area for the instructions so they stay within the square box without stretching downwards.
- **Improved Layout**: Increased internal padding to 24dp for a more balanced look.

### 2. Bold Text Enhancements
- **Title**: Updated to `FontWeight.Black` for a strong visual header.
- **Section Headers**: Set to `FontWeight.Bold` and `FontWeight.Black` to clearly separate different parts of the guide.
- **Instructions**: Increased all body text to `FontWeight.SemiBold` or `FontWeight.Bold` to ensure clear visibility against the glass background.
- **Steps Numbers**: Made the step numbers extra bold and colored them **green** (`0xFF00E676`) to make the list easier to follow.
- **TikTok ID**: Enlarged and bolded the TikTok handle for better readability.

## Files Updated
- [ConnectionHelpDialog.kt](file:///E:/Project App Juken/Juken 5 Live Monitor App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/Connect/components/ConnectionHelpDialog.kt)

## How to Verify
1. Open the application.
2. Tap the **question mark icon (?)** in the top-right corner of the connection screen.
3. Observe the new **Square** shape of the popup.
4. Verify that the text is **bolder** and clearly legible.
5. Scroll through the instructions to ensure the internal scrolling works smoothly.

> [!TIP]
> The fixed square size ensures a consistent look across different device screen sizes and prevents the dialog from overlapping with system navigation bars on smaller devices.

render_diffs(file:///E:/Project App Juken/Juken 5 Live Monitor App/Juken5LiveMonitor/app/src/main/java/com/kkr/juken5livemonitor/screen/Connect/components/ConnectionHelpDialog.kt)
