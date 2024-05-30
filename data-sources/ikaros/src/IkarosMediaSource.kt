import adapt.IkarosPagingSizedSourceAdapt;
import kotlin.coroutines.Continuation;
import me.him188.ani.datasources.api.paging.SizedSource;
import me.him188.ani.datasources.api.source.ConnectionStatus;
import me.him188.ani.datasources.api.source.MediaFetchRequest;
import me.him188.ani.datasources.api.source.MediaMatch;
import me.him188.ani.datasources.api.source.MediaSource;
import me.him188.ani.datasources.api.source.MediaSourceKind;

import me.him188.ani.datasources.api.source.MediaSourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IkarosMediaSource implements MediaSource {
    public static final String ID = "Ikaros";
    private static final String BASE_URL = GetEnv("ANI_DS_IKAROS_BASE_URL");
    private static final String USERNAME = GetEnv("ANI_DS_IKAROS_USERNAME");
    private static final String PASSWORD = GetEnv("ANI_DS_IKAROS_PASSWORD");
    private IkarosClient ikarosClient = new IkarosClient(BASE_URL, USERNAME, PASSWORD);

    private static String GetEnv(String envName) {
        if (envName == null || envName.isEmpty()) {
            return "";
        }
        String env = System.getenv(envName);
        if (env == null || env.isEmpty()) {
            return "";
        }
        return env;
    }

    @NotNull
    @Override
    public String getMediaSourceId() {
        return ID;
    }

    @NotNull
    @Override
    public MediaSourceKind getKind() {
        return MediaSourceKind.WEB;
    }

    @Nullable
    @Override
    public ConnectionStatus checkConnection(@NotNull Continuation<? super ConnectionStatus> $completion) {
        return (200 == ikarosClient.checkConnection()) 
                ? ConnectionStatus.SUCCESS : ConnectionStatus.FAILED;
    }

    @Nullable
    @Override
    public SizedSource<MediaMatch> fetch(@NotNull MediaFetchRequest query, @NotNull Continuation<? super SizedSource<? extends MediaMatch>> $completion) {
        return new IkarosPagingSizedSourceAdapt<>(ikarosClient.getSubjectMediaMatchs(query));
    }

    @NotNull
    @Override
    public MediaSourceLocation getLocation() {
        return MediaSourceLocation.Online.INSTANCE;
    }
}
