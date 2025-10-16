import openpyxl
import re
import os
import sys
sys.stdout.reconfigure(encoding='utf-8')

I18N_PATHS = [
    "src/main/resources/i18n",                      # Module hiện tại
]

OUTPUT_PATH = "src/main/java/com/wis/i18n/TranslateCommon.java"
PACKAGE_NAME = "com.wis.i18n"


def to_enum_name(key: str) -> str:
    """Chuyển key thành tên enum hợp lệ"""
    s = re.sub(r"[^A-Za-z0-9]", "_", key)
    s = re.sub(r"_+", "_", s).upper().strip("_")
    if not s:
        s = "KEY"
    if s[0].isdigit():
        s = "K_" + s
    return s


def main():
    keys = set()
    total_files = 0

    for i18n_dir in I18N_PATHS:
        if not os.path.isdir(i18n_dir):
            print(f"Không tìm thấy thư mục: {i18n_dir}")
            continue

        print(f"Đang quét thư mục: {i18n_dir}")
        for filename in os.listdir(i18n_dir):
            if not filename.endswith(".xlsx") or filename.startswith("~$"):
                continue

            total_files += 1
            full_path = os.path.join(i18n_dir, filename)
            print(f"Đọc file: {full_path}")

            try:
                wb = openpyxl.load_workbook(full_path)
                sheet = wb.active
                for row in sheet.iter_rows(min_row=2, max_col=1, values_only=True):
                    key = row[0]
                    if key and str(key).strip():
                        keys.add(str(key).strip())
            except Exception as e:
                print(f"Lỗi khi đọc {filename}: {e}")

    if not keys:
        print("Không tìm thấy key nào trong các file i18n")
        return

    os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)
    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        f.write(f"package {PACKAGE_NAME};\n\n")
        f.write("import java.util.Map;\nimport java.util.concurrent.ConcurrentHashMap;\nimport lombok.Getter;\nimport lombok.AllArgsConstructor;\nimport lombok.ToString;\n\n")
        f.write("@Getter\n")
        f.write("@AllArgsConstructor\n")
        f.write("@ToString\n")
        f.write("public enum TranslateCommon {\n\n")

        for i, key in enumerate(sorted(keys)):
            enum_name = to_enum_name(key)
            sep = ";" if i == len(keys) - 1 else ","
            f.write(f'    {enum_name}("{key}"){sep}\n')

        f.write("\n    private final String key;\n")
        f.write("    private static final Map<String, TranslateCommon> BY_KEY = new ConcurrentHashMap<>();\n")
        f.write("    static { for (TranslateCommon e : values()) BY_KEY.put(e.key, e); }\n")
        f.write("    public static TranslateCommon fromKey(String key) { return BY_KEY.get(key); }\n")
        f.write("}\n")

    print(f"\nĐã sinh file: {OUTPUT_PATH}")
    print(f"Tổng số key: {len(keys)} (từ {total_files} file Excel)")


if __name__ == "__main__":
    main()
