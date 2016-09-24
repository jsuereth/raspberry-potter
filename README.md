# Harry Potter wand automation

This project aims to detect motion from IR sources (or reflectors), classify into a "shape" and fire events for home automation.

## Hardware

- [Raspberry PI 3](https://www.raspberrypi.org/products/raspberry-pi-3-model-b/)
- [IR Positioning Camera](http://www.dfrobot.com/index.php?route=product/product&product_id=1088)
- *Pending*
  - IR LEDs
  - Microphone and/or motion sensor

## Basic Software Design

- Upon sound/motion sensing, IR LEDs blast IR from the raspberry PI unit and/or "wand" device
- Generate an image of last 'N' positions for each tracked IR source
- Use OpenCV to classify image against trained 'spell shapes'.  Fire an event when a spell shape is above a confidence threshold.
- Call the configured event handler for a given spell.

## Physical design

*TODO - fill this in once we've built a housing for the unit*


## Spell Handlers

*TODO figure out what these look like and how to configure them*.

Ideas
- LED control (on/off)
- Play audio file/audio stream (on/off)
- Remote automation API calls (wink/nest, etc.)
  - Light Switch
  - Lock/Unlock 

# Task list

- [X] Basic IR tracking camera controls, sensitivity settings, etc.
- [ ] local LED controller
- [ ] Motion/Sound activated IR LED
- [ ] OpenCV model training
- [ ] Home automation API hooks
- [ ] Local Audio controller
- [ ] Physical housing


# Licensing

Still investigating what to use for this.

