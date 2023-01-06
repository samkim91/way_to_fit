import 'dart:async';

import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:way_to_fit/src/domain/entities/participation_type.dart';

import '../../../core/config/logger.dart';
import '../../../data/models/wod.dart';
import '../../../domain/entities/wod_type.dart';
import '../../../domain/repositories/wod_repository.dart';

part 'wod_create_event.dart';
part 'wod_create_state.dart';

class WodCreateBloc extends Bloc<WodCreateEvent, WodCreateState> {
  final WodRepository _wodRepository;

  WodCreateBloc(this._wodRepository) : super(const WodCreateState()) {
    on<SelectWodType>(_onChangeWodType);
    on<TypeWodTypeDetail>(_onChangeWodTypeDetail);
    on<SelectParticipationType>(_onSelectParticipationType);
    on<TypeMemberCount>(_onChangeMemberCount);
    on<TypeWodMovement>(_onChangeWodMovement);
    on<AddWodMovement>(_onAddWodMovement);
    on<ClickDeleteWodMovement>(_onDeleteWodMovement);
    on<SaveWod>(_onSaveWod);
  }

  FutureOr<void> _onChangeWodType(
      SelectWodType event, Emitter<WodCreateState> emit) {
    logger.d('_wodTypeChanged ${event.wodType}');

    final Wod updatedWod = state.wod.copyWith(type: event.wodType);

    emit(state.copyWith(wod: updatedWod));
  }

  FutureOr<void> _onChangeWodTypeDetail(
      TypeWodTypeDetail event, Emitter<WodCreateState> emit) {
    logger.d('_wodTypeSubChanged ${event.wodTypeSub}');

    final String wodTypeSub = event.wodTypeSub;
    final Wod updatedWod = state.wod.copyWith(typeDetail: wodTypeSub);

    emit(state.copyWith(
        wod: updatedWod, isValidTypeDetail: wodTypeSub.isNotEmpty));
  }

  FutureOr<void> _onSelectParticipationType(
      SelectParticipationType event, Emitter<WodCreateState> emit) {
    logger.d('_participationTypeSelected: ${event.participationType}');

    final Wod updatedWod =
        state.wod.copyWith(participationType: event.participationType);

    emit(state.copyWith(wod: updatedWod));
  }

  FutureOr<void> _onChangeMemberCount(
      TypeMemberCount event, Emitter<WodCreateState> emit) {
    logger.d('_participationTypeSelected: ${event.memberCount}');

    final Wod updatedWod = state.wod.copyWith(memberCount: event.memberCount);

    emit(state.copyWith(wod: updatedWod));
  }

  FutureOr<void> _onChangeWodMovement(
      TypeWodMovement event, Emitter<WodCreateState> emit) {
    logger.d('_wodDetailChanged: ${event.wodDetail}');

    emit(state.copyWith(movement: event.wodDetail));
  }

  FutureOr<void> _onAddWodMovement(
      AddWodMovement event, Emitter<WodCreateState> emit) {
    final String wodDetail = event.wodDetail;

    logger.d('_wodDetailAdded: $wodDetail');

    if (wodDetail.isEmpty) {
      return Future.sync(() => null);
    }

    List<String> wodDetails = List.from(state.wod.movements)..add(wodDetail);

    final Wod updatedWod = state.wod.copyWith(movements: wodDetails);

    emit(state.copyWith(
        wod: updatedWod, isValidMovements: wodDetails.isNotEmpty));
  }

  FutureOr<void> _onDeleteWodMovement(
      ClickDeleteWodMovement event, Emitter<WodCreateState> emit) {
    logger.d('_wodDetailDeleted: ${event.index}');

    List<String> wodDetails = List.from(state.wod.movements)
      ..removeAt(event.index);

    final Wod updatedWod = state.wod.copyWith(movements: wodDetails);

    emit(state.copyWith(
        wod: updatedWod, isValidMovements: wodDetails.isNotEmpty));
  }

  FutureOr<void> _onSaveWod(SaveWod event, Emitter<WodCreateState> emit) async {
    logger.d('_wodCreate: ');

    // Validation
    if (state.wod.typeDetail.isEmpty || state.wod.movements.isEmpty) {
      emit(state.copyWith(
          isValidTypeDetail: state.wod.typeDetail.isNotEmpty,
          isValidMovements: state.wod.movements.isNotEmpty));

      return Future.sync(() => null);
    }

    try {
      emit(state.copyWith(status: WodCreateStatus.processing));
      await _wodRepository.createWod(state.wod);
      emit(state.copyWith(status: WodCreateStatus.success));
    } catch (e) {
      emit(state.copyWith(status: WodCreateStatus.error, error: e.toString()));
    }
  }
}
