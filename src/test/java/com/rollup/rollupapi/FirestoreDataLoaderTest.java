package com.rollup.rollupapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.rollup.rollupapi.config.FirebaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.util.Map;

/**
 * Firestore 데이터 로더 테스트
 *
 * 사용법:
 * 1. resources/firestore_file/ 에 JSON 파일 추가
 *    - 파일명 = 컬렉션명 (예: game_list.json → game_list 컬렉션)
 *    - JSON 키 = 도큐먼트 ID
 *    - 하위 키값 = 필드
 *
 * 2. 원하는 테스트 메서드 실행
 */
@SpringBootTest(classes = {FirebaseConfig.class, ObjectMapper.class})
@ActiveProfiles("test")
class FirestoreDataLoaderTest {

    @Autowired
    private Firestore firestore;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_PATH = "firestore_file/";

    /**
     * game_list 컬렉션 데이터 삽입
     */
    @Test
    void loadGameList() throws Exception {
        loadJsonToFirestore("game_list.json");
    }

    /**
     * 범용 로더 - 파일명 지정해서 실행
     * 필요시 파일명만 바꿔서 복사해서 쓰면 됨
     */
    @Test
    void loadCustomFile() throws Exception {
        // 여기 파일명만 바꿔서 실행
        loadJsonToFirestore("game_list.json");
    }

    /**
     * JSON 파일을 읽어서 Firestore에 삽입
     * - 파일명(확장자 제외) = 컬렉션명
     * - JSON 키 = 도큐먼트 ID
     * - 하위 객체 = 필드들
     */
    private void loadJsonToFirestore(String fileName) throws Exception {
        String collectionName = fileName.replace(".json", "");

        System.out.println("===========================================");
        System.out.println("컬렉션: " + collectionName);
        System.out.println("파일: " + BASE_PATH + fileName);
        System.out.println("===========================================");

        // JSON 파일 읽기
        InputStream inputStream = new ClassPathResource(BASE_PATH + fileName).getInputStream();
        Map<String, Map<String, Object>> documents = objectMapper.readValue(
                inputStream,
                new TypeReference<>() {}
        );

        // Firestore에 배치 삽입
        WriteBatch batch = firestore.batch();

        for (Map.Entry<String, Map<String, Object>> entry : documents.entrySet()) {
            String documentId = entry.getKey();
            Map<String, Object> fields = entry.getValue();

            batch.set(
                    firestore.collection(collectionName).document(documentId),
                    fields
            );
            System.out.println("  ✓ " + documentId);
        }

        batch.commit().get();

        System.out.println("===========================================");
        System.out.println("완료! " + documents.size() + "건 삽입");
        System.out.println("===========================================");
    }
}
