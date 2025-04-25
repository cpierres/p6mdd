import {PostCommentDto} from './PostCommentDto';

export interface PostDto {
  id: string;
  title: string;
  topicId: string;
  topicTitle: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  createdByUsername: string;
  updatable: boolean;
  comments?: PostCommentDto[];
}
