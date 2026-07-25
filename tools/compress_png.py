import os
import sys
import argparse
from PIL import Image


def compress_png(input_path, quality=80, max_width=None):
    try:
        img = Image.open(input_path)
        if img.format != 'PNG':
            print(f"  跳过非PNG文件: {os.path.basename(input_path)}")
            return 0, 0

        original_size = os.path.getsize(input_path)
        original_width, original_height = img.size

        if max_width and original_width > max_width:
            ratio = max_width / original_width
            new_height = int(original_height * ratio)
            img = img.resize((max_width, new_height), Image.LANCZOS)
            print(f"    尺寸调整: {original_width}x{original_height} -> {max_width}x{new_height}")

        output_path = input_path
        img.save(output_path, format='PNG', optimize=True, quality=quality)

        compressed_size = os.path.getsize(output_path)
        saved = original_size - compressed_size
        ratio = (saved / original_size) * 100 if original_size > 0 else 0

        print(f"  压缩完成: {os.path.basename(input_path)}")
        print(f"    原始大小: {format_size(original_size)}")
        print(f"    压缩后: {format_size(compressed_size)}")
        print(f"    节省: {format_size(saved)} ({ratio:.1f}%)")

        return original_size, compressed_size

    except Exception as e:
        print(f"  压缩失败 {os.path.basename(input_path)}: {str(e)}")
        return 0, 0


def format_size(bytes_size):
    if bytes_size < 1024:
        return f"{bytes_size} B"
    elif bytes_size < 1024 * 1024:
        return f"{bytes_size / 1024:.1f} KB"
    else:
        return f"{bytes_size / (1024 * 1024):.2f} MB"


def process_directory(dir_path, quality=80, max_width=None):
    total_original = 0
    total_compressed = 0
    file_count = 0

    for root, dirs, files in os.walk(dir_path):
        for file in files:
            if file.lower().endswith('.png'):
                file_path = os.path.join(root, file)
                original, compressed = compress_png(file_path, quality, max_width)
                total_original += original
                total_compressed += compressed
                file_count += 1

    return file_count, total_original, total_compressed


def main():
    parser = argparse.ArgumentParser(description='压缩PNG图片大小')
    parser.add_argument('path', help='PNG文件路径或包含PNG文件的目录')
    parser.add_argument('-q', '--quality', type=int, default=80,
                        help='压缩质量 (1-100, 默认80)')
    parser.add_argument('-w', '--max-width', type=int, default=None,
                        help='最大宽度，超过则按比例缩小（像素）')
    args = parser.parse_args()

    input_path = args.path
    quality = max(1, min(100, args.quality))
    max_width = args.max_width

    if not os.path.exists(input_path):
        print(f"错误: 路径不存在: {input_path}")
        sys.exit(1)

    print(f"PNG压缩工具 - 质量: {quality}" + (f", 最大宽度: {max_width}px" if max_width else ""))
    print(f"目标路径: {input_path}")
    print("-" * 50)

    if os.path.isfile(input_path):
        if input_path.lower().endswith('.png'):
            compress_png(input_path, quality, max_width)
        else:
            print("错误: 请提供PNG文件或目录")
            sys.exit(1)
    elif os.path.isdir(input_path):
        file_count, total_original, total_compressed = process_directory(input_path, quality, max_width)
        print("-" * 50)
        print(f"总计处理: {file_count} 个PNG文件")
        print(f"原始总大小: {format_size(total_original)}")
        print(f"压缩后总大小: {format_size(total_compressed)}")
        if total_original > 0:
            saved = total_original - total_compressed
            ratio = (saved / total_original) * 100
            print(f"总节省: {format_size(saved)} ({ratio:.1f}%)")


if __name__ == '__main__':
    main()
