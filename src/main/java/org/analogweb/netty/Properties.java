package org.analogweb.netty;

import java.io.File;

import io.netty.handler.ssl.SslContext;
import org.analogweb.ApplicationProperties;
import org.analogweb.ServerFactoryImpl;
import org.analogweb.util.StringUtils;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;

public class Properties {

    private static final Log log = Logs
            .getLog(Properties.class);
    private static final int DEFAULT_AGGREGATION_SIZE = 10485760;
    private static final int DEFAULT_PARALLELISM = Runtime.getRuntime()
            .availableProcessors();
    private static final String SCHEDULE_TIMEOUT_LIMIT_KEY = "analogweb.netty.schedule-timeout-limit";
    private static final String MAX_AGGREGATION_SIZE_KEY = "analogweb.netty.max-aggregation-size";
    private static final String EXECUTOR_PARALLELISM_KEY = "analogweb.netty.parallelism";
    private static final String SSL_KEY = "analogweb.netty.ssl";
    private static final String SSL_CONTEXT_KEY = "analogweb.netty.ssl.context";
    private static final String SSL_PRIVATE_KEY_KEY = "analogweb.netty.ssl.private-key";
    private static final String SSL_CERTIFICATE_KEY = "analogweb.netty.ssl.certificate";
    private static final String SSL_PASSPHRASE_KEY = "analogweb.netty.ssl.passphrase";

    public static SslContext getSslContext(ApplicationProperties props) {
        Object obj = props.getProperties().get(SSL_CONTEXT_KEY);
        if (obj instanceof SslContext) {
            return (SslContext) obj;
        } else {
            return null;
        }
    }

    public static int getMaxAggregationSize(
            ApplicationProperties applicationProperties) {
        String size = System.getProperty(MAX_AGGREGATION_SIZE_KEY);
        if (StringUtils.isEmpty(size)) {
            size = applicationProperties
                    .getStringProperty(MAX_AGGREGATION_SIZE_KEY);
        }
        if (StringUtils.isNotEmpty(size)) {
            return parseInt(size, MAX_AGGREGATION_SIZE_KEY, DEFAULT_AGGREGATION_SIZE);
        }
        return DEFAULT_AGGREGATION_SIZE;
    }

    public static boolean isSSL() {
        return System.getProperty(SSL_KEY) != null;
    }

    public static int getExecutorParallelism() {
        String parallelism = System.getProperty(EXECUTOR_PARALLELISM_KEY);
        return parseInt(parallelism, EXECUTOR_PARALLELISM_KEY, DEFAULT_PARALLELISM);
    }

    private static int parseInt(String maybeNumber, String key, int defaultValue) {
        try {
            return Integer.parseInt(maybeNumber);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static File getSSLPrivateKey(ApplicationProperties applicationProperties) {
        String privateKeyPath = System.getProperty(SSL_PRIVATE_KEY_KEY);
        return StringUtils.isNotEmpty(privateKeyPath) ? new File(privateKeyPath) : null;
    }

    public static File getSSLCertificate(ApplicationProperties applicationProperties) {
        String certificatePath = System.getProperty(SSL_CERTIFICATE_KEY);
        return StringUtils.isNotEmpty(certificatePath) ? new File(certificatePath) : null;
    }

    public static String getSSLKeyPassPhrase(ApplicationProperties applicationProperties) {
        return System.getProperty(SSL_PASSPHRASE_KEY);
    }

    public static int getScheduleTimeoutLimit() {
        String limit = System.getProperty(SCHEDULE_TIMEOUT_LIMIT_KEY);
        return parseInt(limit,SCHEDULE_TIMEOUT_LIMIT_KEY,1);
    }

}
