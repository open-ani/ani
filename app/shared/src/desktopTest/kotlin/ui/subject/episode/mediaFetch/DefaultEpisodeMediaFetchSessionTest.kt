package me.him188.ani.app.ui.subject.episode.mediaFetch

//class DefaultEpisodeMediaFetchSessionTest {
//    private val episodePreferences: DataStore<Preferences> = MemoryDataStore(mutablePreferencesOf())
//    private val defaultMediaPreferences: MutableStateFlow<MediaPreference> = MutableStateFlow(MediaPreference.Empty)
//    private val mediaSelectorSettings = MutableStateFlow(MediaSelectorSettings())
//    private val subjectCompleted = MutableStateFlow(false)
//    private val mediaSourceInstances = MutableStateFlow<List<MediaSourceInstance>>(emptyList())
//
//    private fun createSession(
//        subjectInfo: SubjectInfo = SubjectInfo(),
//    ): DefaultEpisodeMediaFetchSession {
//        return DefaultEpisodeMediaFetchSession(
//            subjectId = 100,
//            subject = flowOf(subjectInfo),
//            episode = flowOf(EpisodeInfo(100)),
//            episodePreferencesRepository = EpisodePreferencesRepositoryImpl(
//                episodePreferences,
//                flowOf(MediaPreference.Empty)
//            ),
//            defaultMediaPreferenceFlow = defaultMediaPreferences,
//            mediaSelectorSettingsFlow = mediaSelectorSettings,
//            subjectCompletedNotCached = subjectCompleted,
//            mediaSourceInstances = mediaSourceInstances,
//            parentCoroutineContext = EmptyCoroutineContext,
//        )
//    }
//
//    @Test
//    fun `can create session`() {
//        createSession()
//    }
//
//    @Test
//    fun `auto fetch media after completion`() = runTest {
//        mediaSourceInstances.value = listOf(
//            createTestMediaSourceInstance(
//                TestHttpMediaSource(fetch = {
//                    SinglePagePagedSource {
//                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
//                    }
//                })
//            )
//        )
//        val session = createSession()
//        cancellableCoroutineScope {
//            launch { // watchdog
//                yield()
//                val fetch = session.mediaFetchSession.first()
//                while (isActive) {
//                    println(fetch.cumulativeResults.first().size)
//                    println(fetch.resultsPerSource.values.first().state.value)
//                    println()
//                    withContext(Dispatchers.Default) { delay(1000) }
//                }
//            }
//            session.doAutoSelectOnFetchCompletion()
//            cancelScope()
//        }
//    }
//}
