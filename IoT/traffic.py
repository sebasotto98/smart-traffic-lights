import os
import logging
import logging.handlers
import random

import numpy as np
import skvideo.io
import cv2
import matplotlib.pyplot as plt

import utils

import time
import paho.mqtt.client as paho

#from gpiozero import LED

# Without this some strange errors happen
cv2.ocl.setUseOpenCL(False)
random.seed(123)

from pipeline import (
    PipelineRunner,
    ContourDetection,
    Visualizer,
    CsvWriter,
    VehicleCounter)

# ============================================================================
IMAGE_DIR = "./out"
VIDEO_SOURCE = "input.mp4"
SHAPE = (720, 1280)  # HxW
EXIT_PTS = np.array([
    [[732, 720], [732, 590], [1280, 500], [1280, 720]]
])
BROKER = "192.168.146.28"
PORT = 1883
TOPIC_W = "TRAFFIC_LIGHT_1_CARS"
TOPIC_R = "TRAFFIC_LIGHT_1_STATE"
vehicle_total = 0
vehicle_green = 0
green_flag = True
#red = LED(22)
#yellow = LED(27)
#green = LED(17)
# ============================================================================

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        client.connected_flag = True  # Set flag
        print("Connected OK")
    else:
        print("Bad connection, returned code = ", rc)

def on_message(client, userdata, message):
    time.sleep(1)
    global green_flag
    global vehicle_green
    message = str(message.payload.decode("utf-8"))
    print("Received message = ", message)
    if 'red' in message:
        green_flag = False
        print('YELLOW')
        print('RED')
        #green.off()
        #yellow.on()
        #sleep(3)
        #yellow.off()
        #red.on()
    elif 'green' in message:
        green_flag = True
        vehicle_green = 0
        print('GREEN')
        #red.off()
        #green.on()
    else:
        print('Wrong message payload received.')

def train_bg_subtractor(inst, cap, num=500):
    '''
        BG substractor need process some amount of frames to start giving result
    '''
    print ('Training BG Subtractor...')
    i = 0
    for frame in cap:
        inst.apply(frame, None, 0.001)
        i += 1
        if i >= num:
            return cap

def main():
    log = logging.getLogger("main")
    global vehicle_total
    global vehicle_green
    paho.Client.connected_flag = False  # Create flag in class
    client = paho.Client("traffic_light_1")  # Create client object
    client.on_message = on_message
    client.on_connect = on_connect  # Bind function to callback
    client.loop_start()
    # Connecting to broker
    print("Connecting to broker", BROKER)
    client.connect(BROKER, PORT)
    while not client.connected_flag: # Wait in loop
        print("In wait loop")
        time.sleep(1)
    print("In main loop")
    # Subscribe
    print("Subscribing")
    client.subscribe(TOPIC_R)

    # Creating exit mask from points, where we will be counting vehicles
    base = np.zeros(SHAPE + (3,), dtype='uint8')
    exit_mask = cv2.fillPoly(base, EXIT_PTS, (255, 255, 255))[:, :, 0]

    # BG substruction
    bg_subtractor = cv2.createBackgroundSubtractorMOG2(
        history=500, detectShadows=True)

    # Processing pipline
    pipeline = PipelineRunner(pipeline=[
        ContourDetection(bg_subtractor=bg_subtractor,
                         save_image=True, image_dir=IMAGE_DIR),
        # We use y_weight == 2.0 because traffic are moving vertically on video
        VehicleCounter(exit_masks=[exit_mask], y_weight=2.0),
        Visualizer(image_dir=IMAGE_DIR),
        CsvWriter(path='./', name='report.csv')
    ], log_level=logging.DEBUG)
    
    # Set up image source
    cap = skvideo.io.vreader(VIDEO_SOURCE)

    # Skipping 500 frames to train BG subtractor
    train_bg_subtractor(bg_subtractor, cap, num=500)

    _frame_number = -1
    frame_number = -1
    for frame in cap:
        if not frame.any():
            log.error("Frame capture failed, stopping...")
            break

        # Real frame number
        _frame_number += 1

        # Skip every 2nd frame to speed up processing
        if _frame_number % 2 != 0:
            continue

        # Frame number that will be passed to pipline
        frame_number += 1

        pipeline.set_context({
            'frame': frame,
            'frame_number': frame_number,
        })
        while green_flag == False:
            time.sleep(0.1)
        vehicle_count = pipeline.run()
        if vehicle_total != vehicle_count:
            vehicle_total = vehicle_count
            vehicle_green += 1
            # Publishing
            print("Publishing")
            client.publish(TOPIC_W, "Avenida Rovisco Pais/{}".format(str(vehicle_green)))
    client.loop_stop()  # Stop loop
    client.disconnect()  # Disconnect

# ============================================================================

if __name__ == "__main__":
    log = utils.init_logging()

    if not os.path.exists(IMAGE_DIR):
        log.debug("Creating image directory `%s`...", IMAGE_DIR)
        os.makedirs(IMAGE_DIR)

    main()
