import { Component } from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {Router} from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [
    MatButtonModule
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

  constructor(private router:Router){
  }

  register() {
    //console.log("CPI navigate register");
    this.router.navigate(['/auth/register']);
  }
}
