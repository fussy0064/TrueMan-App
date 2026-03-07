import os
from PIL import Image

def resize_icon(source_path, dest_dir):
    try:
        img = Image.open(source_path)
        
        # Center crop the central icon part if necessary, but usually the prompt generates a centered icon.
        # Let's crop it to the center square.
        width, height = img.size
        # The prompt might generate the icon inside a phone frame.
        # Often it centers the icon. Let's just resize the whole image rather than risk bad cropping
        # But wait! I will crop the central 50% to drop the phone "frame"
        crop_size = min(width, height)
        # However, the icon might be 80% or 50% of the image. Let's do a 60% crop center.
        crop_width = int(width * 0.6)
        crop_height = int(height * 0.6)
        
        left = (width - crop_width) / 2
        top = (height - crop_height) / 2
        right = (width + crop_width) / 2
        bottom = (height + crop_height) / 2
        
        cropped_img = img.crop((left, top, right, bottom))
        
        sizes = {
            "mipmap-xxxhdpi": 192,
            "mipmap-xxhdpi": 144,
            "mipmap-xhdpi": 96,
            "mipmap-hdpi": 72,
            "mipmap-mdpi": 48
        }
        
        for folder, size in sizes.items():
            out_img = cropped_img.resize((size, size), Image.ANTIALIAS if hasattr(Image, 'ANTIALIAS') else Image.LANCZOS)
            out_path1 = os.path.join(dest_dir, folder, "ic_launcher.png")
            out_path2 = os.path.join(dest_dir, folder, "ic_launcher_round.png")
            
            os.makedirs(os.path.join(dest_dir, folder), exist_ok=True)
            out_img.save(out_path1)
            out_img.save(out_path2)
            print(f"Saved {size}x{size} to {folder}")
            
    except Exception as e:
        print(f"Error: {e}")

source = "/home/fussy/.gemini/antigravity/brain/a11308f5-077b-4fa6-82af-3dda285f6870/trueman_icon_1772876526681.png"
dest = "/home/fussy/AndroidStudioProjects/TrueMan/app/src/main/res"
resize_icon(source, dest)
