package adapt;

import kotlinx.coroutines.flow.Flow;
import me.him188.ani.datasources.api.paging.SizedSource;

import models.IkarosPagingWrap;

import org.jetbrains.annotations.NotNull;

import static kotlinx.coroutines.flow.FlowKt.flowOf;

public class IkarosPagingSizedSourceAdapt<T> implements SizedSource<T> {
    private final IkarosPagingWrap<T> pagingWrap;

    public IkarosPagingSizedSourceAdapt(IkarosPagingWrap<T> pagingWrap) {
        this.pagingWrap = pagingWrap;
    }

    @NotNull
    @Override
    public Flow<T> getResults() {
        return flowOf(pagingWrap.getItemArray());
    }

    @NotNull
    @Override
    public Flow<Boolean> getFinished() {
        return flowOf(pagingWrap.isLastPage());
    }

    @NotNull
    @Override
    public Flow<Integer> getTotalSize() {
        return flowOf((int) pagingWrap.getTotal());
    }
}
