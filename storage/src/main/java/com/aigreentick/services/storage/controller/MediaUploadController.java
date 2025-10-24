package com.aigreentick.services.storage.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aigreentick.services.storage.constants.MediaConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for media management operations.
 */
@Slf4j
@RestController
@RequestMapping(MediaConstants.Paths.BASE)
@RequiredArgsConstructor
public class MediaUploadController {

}
