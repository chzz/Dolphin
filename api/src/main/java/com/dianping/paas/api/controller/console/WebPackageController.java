package com.dianping.paas.api.controller.console;

import com.dianping.paas.core.dto.request.AllocateWebPackageRequest;
import com.dianping.paas.core.dto.request.DownloadWebPackageRequest;
import com.dianping.paas.core.dto.request.UploadWebPackageRequest;
import com.dianping.paas.core.dto.response.AllocateWebPackageResponse;
import com.dianping.paas.core.dto.response.UploadWebPackageResponse;
import com.dianping.paas.core.message.nats.request.RepositoryRequester;
import com.dianping.paas.repository.service.WebPackageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Created by yuchao on 12/3/15.
 */
@RestController
@RequestMapping("/v1/repository/webpackages")
public class WebPackageController {

    @Resource
    private WebPackageService webPackageService;

    @Resource
    private RepositoryRequester repositoryRequester;


    /**
     * 分配 web package 的上传地址的时候 需要向repository发消息
     */
    @RequestMapping(value = "/allocations", method = RequestMethod.POST)
    public AllocateWebPackageResponse allocateWebPackage(AllocateWebPackageRequest request) {

        return repositoryRequester.allocateWebPackage(request);
    }

    /**
     * 上传 web package 的时候已经落在了本机,所以直接执行,不用nats来发消息
     */
    @RequestMapping(method = RequestMethod.POST)
    public UploadWebPackageResponse uploadWebPackage(UploadWebPackageRequest request) throws IOException {

        return webPackageService.upload(request);
    }

    /**
     * 下载 web package 的时候已经落在了本机,所以直接执行,不用nats来发消息
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource downloadWebPackage(DownloadWebPackageRequest request) {

        return webPackageService.download(request).getFileSystemResource();
    }
}
