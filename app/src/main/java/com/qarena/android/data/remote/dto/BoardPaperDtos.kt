package com.qarena.android.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.qarena.android.model.SubQuestion

data class BoardPaperSummary(
    @SerializedName("exam_id")
    val examId: Int? = null,

    @SerializedName("exam_name")
    val examName: String? = null,

    @SerializedName("academic_level")
    val academicLevel: String? = null,

    @SerializedName("subject_name")
    val subjectName: String? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("board_name")
    val boardName: String? = null,

    @SerializedName("exam_year")
    val examYear: Int? = null,

    @SerializedName("paper_type")
    val paperType: String? = null,

    @SerializedName("group")
    val group: String? = null,

    @SerializedName("total_marks")
    val totalMarks: Int? = null,

    @SerializedName("time")
    val time: String? = null,

    @SerializedName("question_count")
    val questionCount: Int? = null
)

data class BoardPaperListResponse(
    @SerializedName("papers")
    val papers: List<BoardPaperSummary> = emptyList()
)

data class BoardPaperDetailResponse(
    @SerializedName("exam")
    val exam: BoardPaperExam? = null,

    @SerializedName("questions")
    val questions: List<Question> = emptyList()
)

data class BoardPaperExam(
    @SerializedName("exam_id")
    val examId: Int? = null,

    @SerializedName("exam_name")
    val examName: String? = null,

    @SerializedName("academic_level")
    val academicLevel: String? = null,

    @SerializedName("subject_name")
    val subjectName: String? = null,

    @SerializedName("subject_code")
    val subjectCode: String? = null,

    @SerializedName("board_name")
    val boardName: String? = null,

    @SerializedName("exam_year")
    val examYear: Int? = null,

    @SerializedName("paper_type")
    val paperType: String? = null,

    @SerializedName("group")
    val group: String? = null,

    @SerializedName("total_marks")
    val totalMarks: Int? = null,

    @SerializedName("time")
    val time: String? = null
)

data class Question(
    @SerializedName(value = "id", alternate = ["question_id"])
    val id: Int? = null,

    @SerializedName("question_no")
    val questionNo: String? = null,

    @SerializedName("question_text")
    val questionText: String? = null,

    @SerializedName("marks")
    val marks: Double? = null,

    @SerializedName("topic")
    val topic: String? = null,

    @SerializedName("section")
    val section: String? = null,

    @SerializedName("question_type")
    val questionType: String? = null,

    @SerializedName("instruction")
    val instruction: String? = null,

    @SerializedName("word_box")
    val wordBox: WordBox? = null,

    @SerializedName("table_data")
    val tableData: TableData? = null,

    @SerializedName("diagram_required")
    val diagramRequired: Boolean? = null,

    @SerializedName("diagram_type")
    val diagramType: String? = null,

    @SerializedName("diagram_description")
    val diagramDescription: String? = null,

    @SerializedName("diagram_svg")
    val diagramSvg: String? = null,

    @SerializedName("sub_questions")
    val subQuestions: List<SubQuestion>? = null
)

data class TableData(
    @SerializedName("columns")
    val columns: List<String> = emptyList(),

    @SerializedName("rows")
    val rows: List<List<String>> = emptyList()
)

data class WordBox(
    @SerializedName("words")
    val words: List<String> = emptyList()
)