import {Injectable} from '@angular/core';
import {environment} from '../../../../environments/environment';
import {HttpClient, HttpParams} from '@angular/common/http';
import {PostDto} from '../interface/PostDto';
import {Observable, map} from 'rxjs';
import {TopicStatsDto} from '../interface/TopicStatsDto';
import {PostCommentDto} from '../interface/PostCommentDto';
import {ApiResult} from '../../../shared/interfaces/ApiResult';

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = environment.backendUrl + 'posts';
  private topicsApiUrl = environment.backendUrl + 'topics';

  constructor(private http: HttpClient) {
  }

  createPost(post: PostDto): Observable<PostDto> {
    return this.http.post<ApiResult<PostDto>>(this.apiUrl, post)
      .pipe(
        map(apiResult => {
          if (!apiResult.data) {
            throw new Error('data Post non trouvé dans la réponse');
          }
          return apiResult.data;
        })
      );
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

    // Si topicId est 'all' et qu'aucun sortBy n'est défini, on utilise 'all' comme valeur par défaut
    if (topicId === 'all' && !sortBy) {
      params = params.set('sortBy', 'all');
    }

    return this.http.get<PostDto[]>(this.apiUrl, {params});
  }

  getPostById(id: string): Observable<PostDto> {
    return this.http.get<ApiResult<PostDto>>(`${this.apiUrl}/${id}`)
      .pipe(
        map(apiResult => {
          if (!apiResult.data) {
            throw new Error('data Post non trouvé dans la réponse');
          }
          return apiResult.data;
        })
      );
  }

  createComment(comment: PostCommentDto): Observable<PostCommentDto> {
    return this.http.post<ApiResult<PostCommentDto>>(`${environment.backendUrl}comments`, comment)
      .pipe(
        map(apiResult => {
          if (!apiResult.data) {
            throw new Error('data Comment non trouvé dans la réponse');
          }
          return apiResult.data;
        })
      );
  }

  /**
   * Exporte les posts actuellement affichés vers un fichier JSON
   * @param sortBy Critère de tri
   * @param topicId ID du topic (optionnel)
   * @param filename Nom du fichier (optionnel)
   * @returns Observable contenant le chemin du fichier créé
   */
  exportPostsToJson(sortBy?: string | null, topicId?: string | null, filename?: string): Observable<string> {
    let params = new HttpParams();

    if (sortBy) {
      params = params.set('sortBy', sortBy);
    }

    if (topicId && topicId !== 'subscribed' && topicId !== 'all') {
      params = params.set('topicId', topicId);
    }

    if (topicId === 'all' && !sortBy) {
      params = params.set('sortBy', 'all');
    }

    if (filename) {
      params = params.set('filename', filename);
    }

    return this.http.get<ApiResult<string>>(`${this.apiUrl}/export`, {params})
      .pipe(
        map(apiResult => {
          if (!apiResult.data) {
            throw new Error('Chemin du fichier non trouvé dans la réponse');
          }
          return apiResult.data;
        })
      );
  }
}
