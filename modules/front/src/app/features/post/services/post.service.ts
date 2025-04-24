import {Injectable} from '@angular/core';
import {environment} from '../../../../environments/environment';
import {HttpClient, HttpParams} from '@angular/common/http';
import {PostDto} from '../interface/PostDto';
import {Observable} from 'rxjs';
import {TopicStatsDto} from '../interface/TopicStatsDto';
import {PostCommentDto} from '../interface/PostCommentDto';

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

  getPosts(sortBy?: string | null, topicId?: string | null): Observable<PostDto[]> {
    let params = new HttpParams();

    if (sortBy) {
      params = params.set('sortBy', sortBy);
    }

    // Si topicId est 'subscribed' ou 'all', on ne l'ajoute pas aux paramètres
    // 'subscribed': le backend utilisera getAllPostsSubscribed() par défaut
    // 'all': le backend utilisera getAllPosts() quand sortBy='all'
    if (topicId && topicId !== 'subscribed' && topicId !== 'all') {
      params = params.set('topicId', topicId);
    }

    // Si topicId est 'all', on définit sortBy à 'all' pour obtenir tous les posts
    if (topicId === 'all') {
      params = params.set('sortBy', 'all');
    }

    return this.http.get<PostDto[]>(this.apiUrl, {params});
  }

  getPostById(id: string): Observable<PostDto> {
    return this.http.get<PostDto>(`${this.apiUrl}/${id}`);
  }

  createComment(comment: PostCommentDto): Observable<PostCommentDto> {
    return this.http.post<PostCommentDto>(`${environment.baseUrl}comments`, comment);
  }

}
