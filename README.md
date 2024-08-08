# Homework 1 Part 0 – Group 12
# EC500

## Group Members:
1. Henry Xia
2. Phuong Khanh Tran

## Solutions to Each Issue:
### Issue 1 – Location Information:
The application gets the user's location information in 2 ways: real location and synthetic location. We created a toggle button that allow user's to choose which type of location information they want to use for their experience. When the "Use Real Location" button is pressed, it triggers the startLocationUpdates() function. Inside this function, it checks if the application has the necessary location permissions. If granted, the application requests location updates from the device's GPS unit using the Fused Location Provider client. When the "Use Synthetic Location" button is pressed, it triggers the startSyntheticLocationUpdates() function. This function initializes the location's latitude and longitude to 0 and simulates location updates at small regular intervals by increasing the latitude by 0.0025. 


### Issue 2 – Display Speed:
Depends on the user's choice to use real location or synethetic location, the displayed speed are calculated differently. When the user opts to use their real location, the locationCallback is called to handle location updates, and it includes the logic to calculate the speed based on the real location data. On the other hand, using the synthetic location will display a constant speed of 10 miles per hour on the application.


### Issue 3 – Pause Button (Henry Xia):
The implementation for the pause button is simple. The button functions as a toggle switch, allowing the user to pause and resume location updates with a single button click. Pressing the "Pause" button will stop the location and speed updates and changes the button text to "Resume". When the user clicks on the "Resume" button, the application will resume location and speed updates.


### Issue 4 – Color Coding (Henry Xia):
In the functions onSyntheticLocationReceived() and onLocationResult(), we implemented a code segment that alters the color of the displayed speed based on specific speed thresholds. When the speed falls below 10 miles per hour, the display turns green, indicating a slower pace. If the speed ranges from 10 to 20 miles per hour, the display adopts a yellow hue, denoting a moderate speed. Conversely, when the speed exceeds 20 miles per hour, the display shifts to red, signaling a higher velocity.


### Issue 6 – Change Font (Phuong Tran):
To allow user to change the font size for displaying the speed, we integrated a SeekBar widget, which is a control with a draggable thumb that allow user to drag it left and right to control the font size. The seek bar is displayed at the bottom of the application. In MainActivity.java, A SeekBar.OnSeekBarChangeListener is implemented to capture the user's interactions. When the user slides the SeekBar, the onProgressChanged method is triggered, providing the current progress value. The progress value is used to calculate the new font size dynamically. The new font size is calculated by adding 10 to half of the progress value. As the user slides the SeekBar, the font size increases or decreases proportionally. The calculated font size is then applied to the TextView displaying the speed.


### Issue 7 – Help Button (Phuong Tran):
A help button with a "?" logo is located at the top right of the application. Upon pressing the help button, a new dialog box will emerge with clear instructions on navigating the application's features. To create this help button, we customed a XML file, circular_button_background.xml, to develop the circle button. For the displayed instructions, we utilized the AlertDialog module to present the instructional content within the dialog box. Once finished reading the instructions, user can press "OK" to close the dialog box.
