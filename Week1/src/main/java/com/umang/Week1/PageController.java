package com.umang.Week1;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/sse")
    public String ssePage() {
        return "sse.html"; // SSE page
    }

    @GetMapping("/ws")
    public String wsPage() {
        return "websocket.html"; // WebSocket page
    }
}
