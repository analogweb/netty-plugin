package org.analogweb.netty;

import java.io.File;
import java.util.List;

import io.netty.handler.ssl.SslContext;
import org.analogweb.ApplicationProperties;
import org.analogweb.util.StringUtils;
import org.analogweb.util.Version;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;

public class Properties {

    private static final Log log = Logs
            .getLog(Properties.class);
    private static final int DEFAULT_AGGREGATION_SIZE = 10485760;
    private static final int DEFAULT_MAX_CONTENT_LENGTH = 1024 * 100;
    private static final int DEFAULT_PARALLELISM = Runtime.getRuntime()
            .availableProcessors();
    private static final String SCHEDULE_TIMEOUT_LIMIT_KEY = "analogweb.netty.schedule-timeout-limit";
    private static final String MAX_AGGREGATION_SIZE_KEY = "analogweb.netty.max-aggregation-size";
    private static final String MAX_CONTENT_LENGTH_KEY = "analogweb.netty.max-content-length";
    private static final String EXECUTOR_PARALLELISM_KEY = "analogweb.netty.parallelism";
    private static final String HTTP2_KEY = "analogweb.netty.http2";
    private static final String SSL_KEY = "analogweb.netty.ssl";
    private static final String OPENSSL_KEY = "analogweb.netty.openssl";
    private static final String SSL_CONTEXT_KEY = "analogweb.netty.ssl.context";
    private static final String SSL_PRIVATE_KEY_KEY = "analogweb.netty.ssl.private-key";
    private static final String SSL_CERTIFICATE_KEY = "analogweb.netty.ssl.certificate";
    private static final String SSL_PASSPHRASE_KEY = "analogweb.netty.ssl.passphrase";

    private static Properties instance;

    private SslContext sslContext;
    private boolean sslContextInitialized;
    private int maxAggregationSize = Integer.MIN_VALUE;
    private int executorParallelism = Integer.MIN_VALUE;
    private File sSLPrivateKey;
    private boolean sSLPrivateKeyInitialized;
    private File sSLCertificate;
    private boolean sSlCertificateInitialized;
    private int scheduleTimeoutLimit = Integer.MIN_VALUE;
    private int maxContentLength = Integer.MIN_VALUE;
    private String version;

    public static Properties instance() {
        if (instance == null) {
            instance = new Properties();
        }
        return instance;
    }

    private Properties() {
    }

    public SslContext getSslContext(ApplicationProperties props) {
        if (!sslContextInitialized) {
            Object obj = props.getProperties().get(SSL_CONTEXT_KEY);
            if (obj instanceof SslContext) {
                this.sslContext = (SslContext) obj;
            } else {
                this.sslContext = null;
            }
            sslContextInitialized = true;
        }
        return sslContext;
    }

    public int getMaxAggregationSize(
            ApplicationProperties applicationProperties) {
        if (maxAggregationSize == Integer.MIN_VALUE) {
            String size = System.getProperty(MAX_AGGREGATION_SIZE_KEY);
            if (StringUtils.isEmpty(size)) {
                size = applicationProperties
                        .getStringProperty(MAX_AGGREGATION_SIZE_KEY);
            }
            if (StringUtils.isNotEmpty(size)) {
                maxAggregationSize = parseInt(size, MAX_AGGREGATION_SIZE_KEY, DEFAULT_AGGREGATION_SIZE);
            }
            maxAggregationSize = DEFAULT_AGGREGATION_SIZE;
        }
        return maxAggregationSize;
    }

    public boolean isHTTP2() {
        return System.getProperty(HTTP2_KEY) != null;
    }

    public boolean isSSL() {
        return System.getProperty(SSL_KEY) != null || isOpenSSL();
    }

    public boolean isOpenSSL() {
        return System.getProperty(OPENSSL_KEY) != null;
    }

    public int getExecutorParallelism() {
        if (executorParallelism == Integer.MIN_VALUE) {
            String parallelism = System.getProperty(EXECUTOR_PARALLELISM_KEY);
            executorParallelism = parseInt(parallelism, EXECUTOR_PARALLELISM_KEY, DEFAULT_PARALLELISM);
        }
        return executorParallelism;
    }

    private int parseInt(String maybeNumber, String key, int defaultValue) {
        try {
            return Integer.parseInt(maybeNumber);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public File getSSLPrivateKey(ApplicationProperties applicationProperties) {
        if (!sSLPrivateKeyInitialized) {
            String privateKeyPath = System.getProperty(SSL_PRIVATE_KEY_KEY);
            sSLPrivateKey = StringUtils.isNotEmpty(privateKeyPath) ? new File(privateKeyPath) : null;
            sSLPrivateKeyInitialized = true;
        }
        return sSLPrivateKey;
    }

    public File getSSLCertificate(ApplicationProperties applicationProperties) {
        if (!sSlCertificateInitialized) {
            String certificatePath = System.getProperty(SSL_CERTIFICATE_KEY);
            sSLCertificate = StringUtils.isNotEmpty(certificatePath) ? new File(certificatePath) : null;
            sslContextInitialized = true;
        }
        return sSLCertificate;
    }

    public static String getSSLKeyPassPhrase(ApplicationProperties applicationProperties) {
        return System.getProperty(SSL_PASSPHRASE_KEY);
    }

    public int getScheduleTimeoutLimit() {
        if (scheduleTimeoutLimit == Integer.MIN_VALUE) {
            String limit = System.getProperty(SCHEDULE_TIMEOUT_LIMIT_KEY);
            scheduleTimeoutLimit = parseInt(limit, SCHEDULE_TIMEOUT_LIMIT_KEY, 1);
        }
        return scheduleTimeoutLimit;
    }

    public int getMaxContentLength() {
        if (maxContentLength == Integer.MIN_VALUE) {
            String limit = System.getProperty(MAX_CONTENT_LENGTH_KEY);
            maxContentLength = parseInt(limit, MAX_CONTENT_LENGTH_KEY, DEFAULT_MAX_CONTENT_LENGTH);
        }
        return maxContentLength;
    }

    public String getVersion() {
        if (version == null) {
            List<Version> vs = Version.load(
                    Thread.currentThread().getContextClassLoader());
            if (vs.isEmpty()) {
                version = "";
            } else {
                version = new StringBuilder().append("/").append(vs.get(0).getVersion()).toString();
            }
        }
        return version;
    }
}
