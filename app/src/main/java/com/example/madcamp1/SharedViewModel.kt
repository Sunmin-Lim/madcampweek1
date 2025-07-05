package com.example.madcamp1

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _players = MutableLiveData<MutableList<Player>>()
    val players: LiveData<MutableList<Player>> get() = _players

    fun setPlayers(list: List<Player>) {
        _players.value = list.toMutableList()
    }

    fun addPlayer(player: Player) {
        val currentList = _players.value?.toMutableList() ?: mutableListOf()
        currentList.add(player)
        _players.value = currentList
    }
}