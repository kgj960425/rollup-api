package com.rollup.rollupapi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    // 방법 1: 파일 경로 (로컬 개발용)
    @Value("${firebase.config-path:firebase-service-account.json}")
    private String firebaseConfigPath;

    // 방법 2: JSON 내용 직접 전달 (배포용)
    @Value("${firebase.credentials:}")
    private String firebaseCredentials;

    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = getFirebaseCredentials();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase 초기화 완료");
        }
    }

    /**
     * Firebase 인증 정보 가져오기
     * 1. 환경변수 FIREBASE_CREDENTIALS 가 있으면 사용 (배포 환경)
     * 2. 없으면 파일에서 읽기 (로컬 개발)
     */
    private InputStream getFirebaseCredentials() throws IOException {
        if (firebaseCredentials != null && !firebaseCredentials.isBlank()) {
            log.info("Firebase 인증: 환경변수 사용");
            return new ByteArrayInputStream(firebaseCredentials.getBytes(StandardCharsets.UTF_8));
        }

        log.info("Firebase 인증: 파일 사용 ({})", firebaseConfigPath);
        return new ClassPathResource(firebaseConfigPath).getInputStream();
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}
