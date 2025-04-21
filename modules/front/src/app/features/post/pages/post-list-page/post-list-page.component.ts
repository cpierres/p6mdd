import {Component} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {Router} from '@angular/router';

@Component({
  selector: 'app-post-list-page',
  imports: [
    MatButtonModule,
  ],
  templateUrl: './post-list-page.component.html',
  styleUrl: './post-list-page.component.scss'
})
export class PostListPageComponent {

  constructor(private router: Router) {
  }

  openCreatePost() {
    // Navigue vers la page de création de post
    this.router.navigate(['/post/create']);
  }
}
