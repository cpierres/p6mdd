import {Injectable} from '@angular/core';
import {environment} from '../../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {PostDto} from '../interface/PostDto';
import {Observable} from 'rxjs';
import {TopicStatsDto} from '../interface/TopicStatsDto';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = environment.baseUrl + 'posts';
  private topicsApiUrl = environment.baseUrl + 'topics';

  constructor(private http: HttpClient) {
  }

  createPost(post: PostDto): Observable<PostDto> {
    return this.http.post<PostDto>(this.apiUrl, post);
  }

  getTopicStats(): Observable<TopicStatsDto[]> {
    return this.http.get<TopicStatsDto[]>(`${this.topicsApiUrl}/stats`);
  }
}
