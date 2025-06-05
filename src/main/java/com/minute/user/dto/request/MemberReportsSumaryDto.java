package com.minute.user.dto.request;

import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.free.dto.response.AdminReportedCommentEntryDTO;
import com.minute.board.free.dto.response.ReportedPostEntryDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberReportsSumaryDto {
       private PageResponseDTO<ReportedPostEntryDTO> reportedPosts;
       private PageResponseDTO<AdminReportedCommentEntryDTO> reportedComments;

        public MemberReportsSumaryDto(
                PageResponseDTO<ReportedPostEntryDTO> reportedPosts,
                PageResponseDTO<AdminReportedCommentEntryDTO> reportedComments
        ) {
            this.reportedPosts = reportedPosts;
            this.reportedComments = reportedComments;
        }

    }


