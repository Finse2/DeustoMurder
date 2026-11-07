
import cv2
from pathlib import Path

# Folder containing this Python script
BASE_DIR = Path(__file__).resolve().parent

# Navigate from AnimatedBackGround to innerProy
PROJECT_DIR = BASE_DIR.parents[3]

# Video in the ressources folder
VIDEO_PATH = PROJECT_DIR / "ressources" / "AnimatedMenu2.mp4"

# Store extracted frames in ressources
OUTPUT_DIR = PROJECT_DIR / "ressources" / "menuFrames2"


def extract_frames():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    video = cv2.VideoCapture(str(VIDEO_PATH))

    if not video.isOpened():
        print(f"Cannot open video: {VIDEO_PATH}")
        return

    fps = video.get(cv2.CAP_PROP_FPS)
    total = int(video.get(cv2.CAP_PROP_FRAME_COUNT))

    print(f"FPS: {fps}")
    print(f"Total frames: {total}")

    frame_number = 0

    while True:
        success, frame = video.read()

        if not success:
            break

        filename = OUTPUT_DIR / f"frame_{frame_number:05d}.jpg"

        cv2.imwrite(
            str(filename),
            frame,
            [cv2.IMWRITE_JPEG_QUALITY, 95]
        )

        frame_number += 1

    video.release()

    print(f"Extracted {frame_number} frames")
    print(f"Output directory: {OUTPUT_DIR}")


if __name__ == "__main__":
    extract_frames()
