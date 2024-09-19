package com.example.sheerhealthinterview

import com.example.sheerhealthinterview.FakeApiService.Companion.listOfCases
import com.example.sheerhealthinterview.network.Case
import com.example.sheerhealthinterview.network.CaseDetails
import com.example.sheerhealthinterview.network.CaseStatus
import com.example.sheerhealthinterview.network.ChatDirection
import com.example.sheerhealthinterview.network.Detail
import com.example.sheerhealthinterview.network.NewCase
import com.example.sheerhealthinterview.network.NewDetail
import com.example.sheerhealthinterview.network.SheerApiService
import com.example.sheerhealthinterview.ui.cases.CasesUiState
import com.example.sheerhealthinterview.ui.cases.CasesViewModel
import com.example.sheerhealthinterview.ui.details.DetailsUiState
import com.example.sheerhealthinterview.ui.details.DetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.Response

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ViewModelTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var casesViewModel: CasesViewModel
    private lateinit var detailsViewModel: DetailsViewModel

    private val exampleCase = FakeApiService.listOfCases[0]

    @Before
    fun setUp() {
        casesViewModel = CasesViewModel(FakeApiService())
        detailsViewModel = DetailsViewModel(exampleCase.caseId, FakeApiService())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_get_cases() = runTest {
        advanceUntilIdle()
        val fakeApiCasesList = listOfCases.map { caseDetail ->
            Case(
                caseDetail.title,
                caseDetail.caseId,
                caseDetail.timestamp,
                caseDetail.status
            )
        }.toMutableList()

        val casesViewModelUiState = casesViewModel.uiState.first()
        val viewModelCasesList = (casesViewModelUiState as CasesUiState.Success).cases
        assertEquals(viewModelCasesList, fakeApiCasesList)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_create_case() = runTest {
        advanceUntilIdle()
        casesViewModel.createCase("new case title", CaseStatus.COMPLETE)
        advanceUntilIdle()
        val fakeApiCasesList = listOfCases.map { caseDetail ->
            Case(
                caseDetail.title,
                caseDetail.caseId,
                caseDetail.timestamp,
                caseDetail.status
            )
        }.toMutableList()

        val casesViewModelUiState = casesViewModel.uiState.first()
        val viewModelCasesList = (casesViewModelUiState as CasesUiState.Success).cases
        assertEquals(viewModelCasesList, fakeApiCasesList)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_delete_case() = runTest {
        advanceUntilIdle()
        casesViewModel.deleteCase(exampleCase.caseId)
        advanceUntilIdle()
        val fakeApiCasesList = listOfCases.map { caseDetail ->
            Case(
                caseDetail.title,
                caseDetail.caseId,
                caseDetail.timestamp,
                caseDetail.status
            )
        }.toMutableList()

        val casesViewModelUiState = casesViewModel.uiState.first()
        val viewModelCasesList = (casesViewModelUiState as CasesUiState.Success).cases
        assertEquals(viewModelCasesList, fakeApiCasesList)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_get_details() = runTest {
        advanceUntilIdle()
        val fakeApiDetail = listOfCases.find {
            it.caseId == exampleCase.caseId
        }
        val detailsViewModelUiState = detailsViewModel.uiState.first()
        val detail = (detailsViewModelUiState as DetailsUiState.Success).details
        assertEquals(fakeApiDetail!!.details, detail)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_create_detail() = runTest {
        advanceUntilIdle()
        val fakeApiDetail = listOfCases.find {
            it.caseId == exampleCase.caseId
        }
        detailsViewModel.sendMessage("hi", fakeApiDetail?.caseId!!)
        advanceUntilIdle()

        val detailsViewModelUiState = detailsViewModel.uiState.first()
        val detail = (detailsViewModelUiState as DetailsUiState.Success).details
        assertEquals(fakeApiDetail.details, detail)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test_delete_detail() = runTest {
        advanceUntilIdle()
        val fakeApiDetail = listOfCases.find {
            it.caseId == exampleCase.caseId
        }
        detailsViewModel.deleteMessage(fakeApiDetail?.caseId!!, fakeApiDetail.details[0].detailId)
        advanceUntilIdle()

        val detailsViewModelUiState = detailsViewModel.uiState.first()
        val detail = (detailsViewModelUiState as DetailsUiState.Success).details
        assertEquals(fakeApiDetail.details, detail)
    }
}

class FakeApiService : SheerApiService {
    companion object {
        val listOfCases = mutableListOf(
            CaseDetails(
                "test case 1",
                "1",
                "timestamp",
                CaseStatus.WAITING_ON_TEAM,
                mutableListOf(
                    Detail(
                        "1",
                        "timestamp",
                        ChatDirection.USER,
                        "hello"
                    )
                )
            ),
            CaseDetails(
                "test case 2",
                "2",
                "timestamp",
                CaseStatus.WAITING_ON_USER,
                mutableListOf(
                    Detail(
                        "1",
                        "timestamp",
                        ChatDirection.USER,
                        "hello"
                    )
                )
            ),
            CaseDetails(
                "test case 3",
                "3",
                "timestamp",
                CaseStatus.COMPLETE,
                mutableListOf(
                    Detail(
                        "1",
                        "timestamp",
                        ChatDirection.USER,
                        "hello"
                    )
                )
            )
        )
    }

    override suspend fun getCases(): Response<MutableList<Case>> {
        return Response.success(listOfCases.map { caseDetail ->
            Case(
                caseDetail.title,
                caseDetail.caseId,
                caseDetail.timestamp,
                caseDetail.status
            )
        }.toMutableList())
    }

    override suspend fun createCase(newCase: NewCase): Response<Case> {
        val newCaseId = listOfCases.size + 1
        val createCaseDetail = CaseDetails(
            newCase.title,
            "$newCaseId",
            "timestamp",
            newCase.status
        )
        listOfCases.add(createCaseDetail)
        return Response.success(
            Case(
                createCaseDetail.title,
                createCaseDetail.caseId,
                createCaseDetail.timestamp,
                createCaseDetail.status
            )
        )
    }

    override suspend fun deleteCase(caseId: String): Response<String> {
        listOfCases.removeIf { case ->
            case.caseId == caseId
        }
        return Response.success("")
    }

    override suspend fun getDetails(caseId: String): Response<CaseDetails> {
        return Response.success(listOfCases.find {
            it.caseId == caseId
        })
    }

    override suspend fun deleteDetail(caseId: String, detailId: String): Response<String> {
        val case = listOfCases.find {
            it.caseId == caseId
        }
        case?.details?.removeIf { detail ->
            detail.detailId == detailId
        }

        return Response.success("")
    }

    override suspend fun createDetail(caseId: String, newDetail: NewDetail): Response<Detail> {
        val case = listOfCases.find {
            it.caseId == caseId
        }
        val newDetailId = (case?.details?.size ?: -1) + 1
        val createDetail = Detail(
            "$newDetailId",
            "timestamp",
            newDetail.direction,
            newDetail.message
        )
        case?.details?.add(createDetail)

        return Response.success(createDetail)
    }
}

@ExperimentalCoroutinesApi
class MainCoroutineRule(private val dispatcher: TestDispatcher = StandardTestDispatcher()) :
    TestWatcher() {

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}