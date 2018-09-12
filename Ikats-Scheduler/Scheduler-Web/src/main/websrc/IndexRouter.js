import React, {Component} from 'react';
import {Route, Link} from 'react-router-dom';
import { inject } from "mobx-react";
import LazyRoute from "lazy-route";

@inject("store")
export default class IndexRouter extends Component
{
    render()
    {
        return(
            <div>
                <Route path="/Login" render={props => (
                    <LazyRoute store={this.props.store.LoginStore} component={import("./Login/LoginContainer")} />
                )}/>

                <Route path="/Job" render={props => (
                    <LazyRoute store={this.props.store.JobStore} component={import("./Job/JobListContainer")} />
                )}/>
                <Route path="/Entrance" render={props => (
                    <LazyRoute store={this.props.store.EntranceStore} component={import("./Entrance/EntranceContainer")} />
                )}/>
                <Route path="/JST" render={props => (
                    <LazyRoute store={this.props.store.EntranceStore} component={import("./JstEntrance/JstEntranceContainer")} />
                )}/>

                <Route path="/Order" render={props => (
                    <LazyRoute store={this.props.store.OrderStore} component={import("./Order/OrderListContainer")} />
                )}/>
                <Route path="/Search" render={props => (
                    <LazyRoute store={this.props.store.SearchStore} component={import("./Search/SearchContainer")} />
                )}/>
            </div>

        );
    }
}