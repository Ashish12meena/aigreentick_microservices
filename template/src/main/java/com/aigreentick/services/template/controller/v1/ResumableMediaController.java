package com.aigreentick.services.template.controller.v1;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import com.aigreentick.services.common.dto.response.ResponseMessage;
import com.aigreentick.services.common.dto.response.ResponseStatus;
import com.aigreentick.services.template.dto.resumable.ResumableMediaUploadResponseDto;
import com.aigreentick.services.template.service.impl.resumablemedia.ResumableMediaUploadServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/template/media")
public class ResumableMediaController {
   private final ResumableMediaUploadServiceImpl mediaUploadService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(@RequestPart("file") MultipartFile file,
            Long userId) {

        ResumableMediaUploadResponseDto responseDto = mediaUploadService.uploadMedia(file, userId);
        return ResponseEntity
                .ok(new ResponseMessage<>(ResponseStatus.SUCCESS.name(), "Media Uploaded Successfully", responseDto));
    }

}
