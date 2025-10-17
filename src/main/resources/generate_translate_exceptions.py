import os
import sys

sys.stdout.reconfigure(encoding="utf-8")

TRANSLATE_FILE = "src/main/java/com/wis/i18n/TranslateCommon.java"
OUTPUT_PATH = "src/main/java/com/wis/i18n/exception/TranslateCommonException.java"
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
        f.write("public class TranslateCommonException extends ServiceException {\n\n")

        f.write("    public TranslateCommonException(HttpStatus status, TranslateCommon translate) {\n")
        f.write("        super(status, translate.name(), false, null, null);\n")
        f.write("    }\n\n")

        f.write("    public TranslateCommonException(HttpStatus status, TranslateCommon translate, List<Object> args) {\n")
        f.write("        super(status, translate.name(), true, null, args);\n")
        f.write("    }\n\n")

        f.write("    public TranslateCommonException(TranslateCommon translate) {\n")
        f.write("        super(HttpStatus.BAD_REQUEST, translate.name(), false, null, null);\n")
        f.write("    }\n\n")

        f.write("    public TranslateCommonException(TranslateCommon translate, List<Object> args) {\n")
        f.write("        super(HttpStatus.BAD_REQUEST, translate.name(), true, null, args);\n")
        f.write("    }\n\n")

        f.write("}\n")

    print(f"Đã sinh file: {OUTPUT_PATH}")
    print("⚙️ TranslateCommonException đã sẵn sàng để sử dụng cùng ServiceException.")


if __name__ == "__main__":
    main()
