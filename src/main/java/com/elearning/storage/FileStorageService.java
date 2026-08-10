package com.elearning.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    // Trả về URL công khai (đường dẫn) để client dùng lại ngay —
    // KHÔNG trả đường dẫn tuyệt đối trên ổ đĩa server (chi tiết implementation
    // không nên lộ ra ngoài).
    String store(MultipartFile file, String subdirectory);
}