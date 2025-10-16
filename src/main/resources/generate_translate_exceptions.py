import os
import sys

sys.stdout.reconfigure(encoding="utf-8")

TRANSLATE_FILE = "src/main/java/com/wis/i18n/TranslateCommon.java"
OUTPUT_PATH = "src/main/java/com/wis/i18n/exception/TranslateException.java"
PACKAGE_NAME = "com.wis.i18n.exception"

def main():
    if not os.path.exists(TRANSLATE_FILE):
        print(f"Không tìm thấy file enum: {TRANSLATE_FILE}")
        return

    os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)

    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        f.write(f"package {PACKAGE_NAME};\n\n")
        f.write("import com.wis.i18n.TranslateCommon;\n")
        f.write("import com.wis.common.exception.ServiceException;\n")
        f.write("import org.springframework.http.HttpStatus;\n")
        f.write("import java.util.List;\n\n")
        f.write("public class TranslateException extends ServiceException {\n\n")

        f.write("    public TranslateException(HttpStatus status, TranslateCommon translate) {\n")
        f.write("        super(status, translate.name(), false, null, null);\n")
        f.write("    }\n\n")

        f.write("    public TranslateException(HttpStatus status, TranslateCommon translate, List<Object> args) {\n")
        f.write("        super(status, translate.name(), true, null, args);\n")
        f.write("    }\n\n")

        f.write("    public TranslateException(TranslateCommon translate) {\n")
        f.write("        super(HttpStatus.BAD_REQUEST, translate.name(), false, null, null);\n")
        f.write("    }\n\n")

        f.write("    public TranslateException(TranslateCommon translate, List<Object> args) {\n")
        f.write("        super(HttpStatus.BAD_REQUEST, translate.name(), true, null, args);\n")
        f.write("    }\n\n")

        f.write("}\n")

    print(f"Đã sinh file: {OUTPUT_PATH}")
    print("⚙️ TranslateException đã sẵn sàng để sử dụng cùng ServiceException.")


if __name__ == "__main__":
    main()
