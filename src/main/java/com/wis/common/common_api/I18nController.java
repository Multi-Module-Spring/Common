package com.wis.common.common_api;

import com.wis.common.exception.ServiceException;
import com.wis.common.util.message.MessageUtil;
import com.wis.i18n.TranslateCommon;
import com.wis.i18n.exception.TranslateCommonException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/i18n")
@RequiredArgsConstructor
public class I18nController {

    private final MessageUtil messageUtil;

    @PostMapping("/import")
    public ResponseEntity<String> importI18n(@RequestParam("file") MultipartFile file) {
        try {
            messageUtil.importExcel(file.getInputStream(), file.getOriginalFilename());
            return ResponseEntity.ok("Import thành công file: " + file.getOriginalFilename());
        } catch (Exception e) {
            throw new TranslateCommonException(HttpStatus.INTERNAL_SERVER_ERROR, TranslateCommon.I18N_IMPORT_ERROR, List.of(e.getMessage()));

        }
    }

    @GetMapping("/export")
    public void exportDynamicKeys(HttpServletResponse response) {
        try {
            Workbook workbook = messageUtil.exportDynamicKeys();

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=i18n_dynamic.xlsx");

            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            throw new TranslateCommonException(HttpStatus.INTERNAL_SERVER_ERROR, TranslateCommon.I18N_EXPORT_ERROR, List.of(e.getMessage()));
        }
    }
}

