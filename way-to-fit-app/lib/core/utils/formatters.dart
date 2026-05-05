import 'package:intl/intl.dart';

final _dateFormatter = DateFormat('yyyy.MM.dd');
final _dateTimeFormatter = DateFormat('MM.dd HH:mm');
final _currencyFormatter = NumberFormat.decimalPattern('ko_KR');

String formatDate(DateTime value) => _dateFormatter.format(value);

String formatDateTime(DateTime value) => _dateTimeFormatter.format(value);

String formatCurrency(int value) => '${_currencyFormatter.format(value)}원';

String formatTimeSeconds(int? value) {
  if (value == null) return '-';
  final minutes = value ~/ 60;
  final seconds = value % 60;
  return '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
}

String formatRelativeDeadline(DateTime value) {
  final diff = value.difference(DateTime.now());
  if (diff.isNegative) return '마감됨';
  if (diff.inDays >= 1) return 'D-${diff.inDays + 1}';
  if (diff.inHours >= 1) return '${diff.inHours}시간 남음';
  if (diff.inMinutes >= 1) return '${diff.inMinutes}분 남음';
  return '곧 마감';
}
