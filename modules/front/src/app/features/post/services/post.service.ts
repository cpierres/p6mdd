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

    // Cas 1: Filtre "Mes thèmes" (subscribed)
    if (topicId === 'subscribed') {
      // Ne pas ajouter de topicId, mais ajouter un paramètre spécial pour indiquer qu'on veut les posts des abonnements
      params = params.set('filterType', 'subscribed');

      // Ajouter le critère de tri s'il existe
      if (sortBy) {
        params = params.set('sortBy', sortBy);
      }
    }
    // Cas 2: Filtre "Tous les thèmes" (all)
    else if (topicId === 'all') {
      // Toujours indiquer qu'on veut tous les posts
      params = params.set('filterType', 'all');

      // Ajouter le critère de tri s'il existe
      if (sortBy) {
        params = params.set('sortBy', sortBy);
      }
    }
    // Cas 3: Filtre par topic spécifique
    else if (topicId) {
      params = params.set('topicId', topicId);

      // Ajouter le critère de tri s'il existe
      if (sortBy) {
        params = params.set('sortBy', sortBy);
      }
    }
    // Cas 4: Aucun filtre spécifié (comportement par défaut)
    else {
      // Ajouter le critère de tri s'il existe
      if (sortBy) {
        params = params.set('sortBy', sortBy);
      }
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

    // Cas 1: Filtre "Mes thèmes" (subscribed)
    if (topicId === 'subscribed') {
      // Ne pas ajouter de topicId, mais ajouter un paramètre spécial pour indiquer qu'on veut les posts des abonnements
      params = params.set('filterType', 'subscribed');

      // Ajouter le critère de tri s'il existe
      if (sortBy) {
        params = params.set('sortBy', sortBy);
      }
    }
    // Cas 2: Filtre "Tous les thèmes" (all)
    else if (topicId === 'all') {
      // Toujours indiquer qu'on veut tous les posts
      params = params.set('filterType', 'all');

      // Ajouter le critère de tri s'il existe
      if (sortBy) {
        params = params.set('sortBy', sortBy);
      }
    }
    // Cas 3: Filtre par topic spécifique
    else if (topicId) {
      params = params.set('topicId', topicId);

      // Ajouter le critère de tri s'il existe
      if (sortBy) {
        params = params.set('sortBy', sortBy);
      }
    }
    // Cas 4: Aucun filtre spécifié (comportement par défaut)
    else {
      // Ajouter le critère de tri s'il existe
      if (sortBy) {
        params = params.set('sortBy', sortBy);
      }
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
