import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ChatComponent} from "./chat/chat.component";
import {AngularHelpComponent} from "./angular-help/angular-help.component";

const routes: Routes = [
  { path: '', redirectTo:'chat', pathMatch: 'full' },
  { path: 'chat', component: ChatComponent },
  { path: 'angular', component: AngularHelpComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
