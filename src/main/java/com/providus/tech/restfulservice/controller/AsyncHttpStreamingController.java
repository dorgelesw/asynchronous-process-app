package com.providus.tech.restfulservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/http-streaming")
public class AsyncHttpStreamingController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ExecutorService service = Executors.newSingleThreadExecutor();


    @GetMapping("/object")
    public ResponseEntity<ResponseBodyEmitter> streamingObject() {
        logger.info("http-streaming object: Request received");

        final ResponseBodyEmitter emitter = new ResponseBodyEmitter(200000l);

        service.execute(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    emitter.send(i + " - ", MediaType.TEXT_PLAIN);
                    logger.info("http-streaming object: Object-" + i + " send");
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.completeWithError(e);
                    return;
                }
            }
            emitter.complete();
        });
        logger.info("http-streaming object: Servlet thread released");
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @GetMapping("/sse")
    public ResponseEntity<SseEmitter> streamingSse() {

        final SseEmitter emitter = new SseEmitter(200000l);
//        final SseEmitter emitter = new SseEmitter();
        logger.info("http-streaming sse: Request received");
        service.execute(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data("SSE MVC-" + i + " " + LocalTime.now().toString(), MediaType.TEXT_EVENT_STREAM)
                            .id(String.valueOf(i))
                            .name("sse event - mvc");
                    emitter.send(event);
                    logger.info("http-streaming sse: event-" + i + " send");
                    Thread.sleep(10000);
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        logger.info("http-streaming sse: Servlet thread released");
        return new ResponseEntity(emitter, HttpStatus.OK);
    }

    @GetMapping("/raw-data-write-bytes")
    public ResponseEntity<StreamingResponseBody> streamingRawData() {
        logger.info("http-streaming raw-data: Request received");
        StreamingResponseBody responseBody = new StreamingResponseBody() {
            @Override
            public void writeTo (OutputStream out) throws IOException {
                try {
                    for (int i = 0; i < 1000; i++) {
                        out.write((Integer.toString(i) + " - ")
                                .getBytes());
                        out.flush();

                        Thread.sleep(1000);
                    }
                }catch (InterruptedException e) {
                    logger.error("http-streaming raw-data: exception", e);
                }

            }
        };
        logger.info("http-streaming raw-data: Servlet thread released");
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    @GetMapping("/raw-data-write-objects")
    public ResponseEntity<StreamingResponseBody> streamingRawDataObject() {
        logger.info("http-streaming raw-data-object: Request received");
        StreamingResponseBody responseBody = outputStream -> {
            Map<String, BigInteger> map = new HashMap<>();
            map.put("one", BigInteger.ONE);
            map.put("ten", BigInteger.TEN);
            try(ObjectOutputStream oos = new ObjectOutputStream(outputStream)){
                oos.writeObject(map);
            }
        };
        logger.info("http-streaming raw-data-object: Servlet thread released");
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    @GetMapping("/raw-data-download-file")
    public ResponseEntity<StreamingResponseBody> streamingRawDataFile() throws FileNotFoundException {
        logger.info("http-streaming raw-data: Request received");
        String fileName = "Covid-19-Nouvelle-Phase";
        File file = ResourceUtils.getFile("classpath:static/" + fileName);
        logger.info("http-streaming raw-data: Servlet thread released");
        StreamingResponseBody responseBody = outputStream -> {
            logger.info("http-streaming raw-data: start write file");
            Files.copy(file.toPath(), outputStream);
            logger.info("http-streaming raw-data: end write file");
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Downloaded_" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(responseBody);
    }

}
