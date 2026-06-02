package cn.ggsn.openrxlight.fsx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import cn.ggsn.openrxlight.lang.Maps2;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@IfBuildProperty(name = "rxlight.fs.type", stringValue = "s3")
@ApplicationScoped
public class S3 implements XFileSystem {
    private final String bucketName;
    private final S3Client s3Client;
    private final FileSystem fileSystem;

    public S3(XFileSystemConfig config) throws IOException {
        this.bucketName = config.bucketName();
        // 创建凭证提供者
        AwsBasicCredentials credentials = AwsBasicCredentials.create(config.accessKey().orElse(null),
                config.secretKey().orElse(null));

        // 初始化 S3 客户端
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(config.endpoint().orElse(null)))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(config.region().orElse(null)))
                .build();
        URI s3Uri = URI.create(config.localBasePath().orElse("/tmp/s3"));
        Path directory = Files.createDirectory(Path.of(s3Uri));
        this.fileSystem = FileSystems.newFileSystem(directory, Maps2.empty());
    }

    @Override
    public void write(String filePath, byte[] content) {
        // Implementation for uploading file to S3
        File file = this.fileSystem.getPath(filePath).toFile();
        file.setWritable(true);
        try {
            Files.write(file.toPath(), content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file to S3", e);
        }
        // s3Client.putObject(builder ->
        // builder.bucket(bucketName).key(filePath).build(),
        // software.amazon.awssdk.core.sync.RequestBody.fromBytes(content));
    }

    @Override
    public void delete(String filePath) {
        // Implementation for deleting file from S3
        s3Client.deleteObject(builder -> builder.bucket(bucketName).key(filePath).build());
    }

    @Override
    public InputStream cat(String filePath) {
        // Implementation for downloading file from S3
        return s3Client.getObject(builder -> builder.bucket(bucketName).key(filePath).build());
    }

    @Override
    public File getFile(String filePath) {
        // Implementation for getting file from S3
        return this.fileSystem.getPath(filePath).toFile();
    }
}
